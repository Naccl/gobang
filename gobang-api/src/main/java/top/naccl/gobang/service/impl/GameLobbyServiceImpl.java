package top.naccl.gobang.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.mapper.ScoreMapper;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;
import top.naccl.gobang.model.entity.Score;
import top.naccl.gobang.rabbitmq.MQSender;
import top.naccl.gobang.service.GameLobbyService;

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


}
