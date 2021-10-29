package top.naccl.gobang.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.manager.SessionManager;
import top.naccl.gobang.mapper.ScoreMapper;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;
import top.naccl.gobang.rabbitmq.MQSender;
import top.naccl.gobang.redis.RedisService;
import top.naccl.gobang.service.GameLobbyService;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-10-29 08:43
 * @description: TODO
 */
@Service
public class GameLobbyServiceImpl implements GameLobbyService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private SimpMessageSendingOperations sender;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private MQSender mqSender;

    @Override
    public void enterRoom(String owner, String username) {
        Room roomOw = RoomManager.getRoomByUsername(owner);
        String player = roomOw.getPlayer();
        // 重连进入房间
        if (!StringUtils.isEmpty(player) && (owner.equals(username) || username.equals(player))) {
            //推送成功消息给此用户
            sender.convertAndSendToUser(username, "/topic/enterRoom", Result.ok(""));
            //推送此消息给房主
            sender.convertAndSendToUser(owner, "/topic/enterRoom", Result.ok("", username));
            //推送此消息给 所有在游戏大厅的用户
            sender.convertAndSend("/topic/enterRoom", Result.ok("", roomOw));
        } else {
            if (RoomManager.isExistUser(username)) {
                //此用户已经创建或进入了一个房间，不可进入其它房间
                sender.convertAndSendToUser(username, "/topic/enterRoom", Result.refuse("已经进入了一个房间，请先退出房间！"));
                return;
            }
            Room room = RoomManager.get(owner);
            if (room.getPlayer() != null) {
                sender.convertAndSendToUser(username, "/topic/enterRoom", Result.refuse("该房间人数已满！"));
                return;
            }
            room.setPlayer(username);
            Game game = GameManager.get(owner);
            game.setPlayer(username);
            //推送成功消息给此用户
            sender.convertAndSendToUser(username, "/topic/enterRoom", Result.ok(""));
            //推送此消息给房主
            sender.convertAndSendToUser(owner, "/topic/enterRoom", Result.ok("", username));
            //推送此消息给 所有在游戏大厅的用户
            sender.convertAndSend("/topic/enterRoom", Result.ok("", room));
        }
    }

    @Override
    public void joinMatching(String username) {
        int score = scoreMapper.findScoreByUsername(username).getScore();
        redisService.set(username, "1");
        if (score >= 0 && score < 100) {
            mqSender.sent_100(username);
        }
    }

    @Override
    public void createRoom(String username) {
        if (RoomManager.isExistUser(username)) {
            //此用户已经创建或进入了一个房间，不可创建其它房间
            sender.convertAndSendToUser(username, "/topic/createRoom", Result.refuse("已经进入了一个房间，请先退出房间！"));
            return;
        }
        Room room = new Room();
        room.setOwner(username);
        RoomManager.add(username, room);
        Game game = new Game();
        game.setOwner(username);
        GameManager.add(username, game);
        //推送成功消息给此用户
        sender.convertAndSendToUser(username, "/topic/createRoom", Result.ok(""));
        //推送此消息给 所有在游戏大厅的用户
        sender.convertAndSend("/topic/createRoom", Result.ok("", room));
    }

    @Override
    public void UnMatching(String username) {
        redisService.del(username);
    }

    /**
     * 用户退出房间
     *
     * @param username 用户名
     */
    @Override
    public void exitRoom(String username) {
        Room room = RoomManager.getRoomByUsername(username);
        //todo 判断游戏是否进行中
        if (username.equals(room.getOwner())) {
            //退出的玩家是房主
            if (room.getPlayer() == null) {
                //房间中不存在第二个玩家，直接移除房间并通知大厅所有人
                RoomManager.remove(username);
                GameManager.remove(username);
                Map<String, Object> map = new HashMap<>();
                map.put("owner", username);
                //房主退出房间后回到大厅，房间数量可能显示不准确
                map.put("roomCount", RoomManager.count());
                sender.convertAndSend("/topic/removeRoom", Result.ok("", map));
            } else {
                //房间中存在第二个玩家，转让房主
                String player = room.getPlayer();
                Room newRoom = new Room();
                newRoom.setOwner(player);
                RoomManager.add(player, newRoom);
                RoomManager.remove(username);
                //通知大厅所有人更新房间信息
                Map<String, Object> map = new HashMap<>();
                map.put("owner", username);
                map.put("room", newRoom);
                sender.convertAndSend("/topic/updateRoom", Result.ok("", map));
                //游戏对局也做同样的处理
                Game newGame = new Game();
                newGame.setOwner(player);
                GameManager.add(player, newGame);
                GameManager.remove(username);
                //todo 告诉第二个玩家 房主已经退出
            }
        } else {
            //退出房间的是第二个玩家，直接退出
            room.setPlayer(null);
            Game game = GameManager.get(room.getOwner());
            game.setPlayer(null);
            game.setPlayerReady(false);
            Map<String, Object> map = new HashMap<>();
            map.put("owner", room.getOwner());
            map.put("room", room);
            //通知大厅所有人更新房间信息
            sender.convertAndSend("/topic/updateRoom", Result.ok("", map));
            //todo 告诉房主 第二个玩家已经退出
        }
    }

    /**
     * 用户订阅此消息后，将 当前在线人数、房间数、房间列表 推送给用户
     *
     */
    @Override
    public Map<String,Object> getRoomList() {
        Map<String, Object> roomList = new HashMap<>();
        roomList.put("playerCount", SessionManager.count());
        roomList.put("roomCount", RoomManager.count());
        roomList.put("roomList", RoomManager.getRoomList());
        return roomList;
    }

    @Override
    public void notifyAllOnlineCount() {
        int playerCount = SessionManager.count();
        sender.convertAndSend("/topic/playerCount", Result.ok("", playerCount));
    }


}
