package top.naccl.gobang.basegame;

import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 17:37
 * @Description:
 */
public abstract class ChessGameOption extends SimpMessageSend
        implements GameOption, GameStateOption, GameInfo {

    @Override
    public Map<String,Object> game(String username) {
        Game game = GameManager.getGameByUsername(username);
        Map<String, Object> map = new HashMap<>();
        map.put("me", username);
        map.put("owner", game.getOwner());
        map.put("player", game.getPlayer());
        map.put("ownerReady", game.isOwnerReady());
        map.put("playerReady", game.isPlayerReady());
        return map;
    }

    @Override
    public void heqi(String owner, String username) {
        Game game = GameManager.get(owner);
        //正在游戏中
        if (game.isPlaying()) {
            //玩家在此对局中
            if (username.equals(game.getOwner())) {
                game.setHeqiUsername(username);
                //向第二玩家发送请求和棋消息
                sender.convertAndSendToUser(game.getPlayer(), "/topic/heqi", Result.ok(""));
            } else if (username.equals(game.getPlayer())) {
                game.setHeqiUsername(username);
                //向房主发送请求和棋消息
                sender.convertAndSendToUser(game.getOwner(), "/topic/heqi", Result.ok(""));
            }
        }
    }

    @Override
    public void agreeHeqi(String owner, String username) {
        Game game = GameManager.get(owner);
        //1.正在游戏中
        //2.玩家在此对局中
        //3.只能同意对方的和棋请求
        if (game.isPlaying() &&
                ((username.equals(game.getOwner()) && game.getPlayer().equals(game.getHeqiUsername()))
                        || (username.equals(game.getPlayer()) && game.getOwner().equals(game.getHeqiUsername())))) {
            String msg = "双方和棋！";
            sender.convertAndSendToUser(game.getOwner(), "/topic/win", Result.ok("", msg));
            sender.convertAndSendToUser(game.getPlayer(), "/topic/win", Result.ok("", msg));
            // 处理对局信息、记录分数
            gameStatistics(game.getOwner(), game.getPlayer(), GameState.PEACE);
            //初始化对局状态
            game.init();
        }
    }

    @Override
    public void refuseHeqi(String owner, String username) {
        Game game = GameManager.get(owner);
        //1.正在游戏中
        //2.玩家在此对局中
        //3.只能拒绝对方的和棋请求
        if (game.isPlaying() &&
                ((username.equals(game.getOwner()) && game.getPlayer().equals(game.getHeqiUsername()))
                        || (username.equals(game.getPlayer()) && game.getOwner().equals(game.getHeqiUsername())))) {
            //发送拒绝和棋消息给申请和棋者
            sender.convertAndSendToUser(game.getHeqiUsername(), "/topic/refuseHeqi", Result.ok("", "对方拒绝和棋"));
            game.setHeqiUsername(null);
        }
    }

    @Override
    public void capitulate(String owner, String username) {
        Game game = GameManager.get(owner);
        //1.正在游戏中
        //2.玩家在此对局中
        String winName = "";
        String loseName = "";
        if (game.isPlaying()) {
            if (username.equals(game.getBlackRole())) {
                String msg = "黑棋认输，白棋获胜！";
                winName = game.getWhiteRole();
                loseName = game.getBlackRole();
                sender.convertAndSendToUser(game.getBlackRole(), "/topic/win", Result.ok("", msg));
                sender.convertAndSendToUser(game.getWhiteRole(), "/topic/win", Result.ok("", msg));
                //初始化对局状态
                game.init();
            } else if (username.equals(game.getWhiteRole())) {
                String msg = "白棋认输，黑棋获胜！";
                winName = game.getBlackRole();
                loseName = game.getWhiteRole();
                sender.convertAndSendToUser(game.getBlackRole(), "/topic/win", Result.ok("", msg));
                sender.convertAndSendToUser(game.getWhiteRole(), "/topic/win", Result.ok("", msg));
                //初始化对局状态
                game.init();
            }
            // 处理对局信息、记录分数
            gameStatistics(winName, loseName, GameState.WINORLOSE);
        }
    }

    @Override
    public void ready(String owner, String username) {
        Game game = GameManager.get(owner);
        //游戏未开始时才可以准备
        if (!game.isPlaying()) {
            if (game.getOwner().equals(username)) {
                //当前准备的玩家是房主
                if (game.isPlayerReady()) {
                    //对手已经准备，直接开始游戏
                    startGame(game);
                    return;
                }
                game.setOwnerReady(true);
                if (game.getPlayer() != null) {
                    //存在第二个玩家，推送准备消息给对方
                    sender.convertAndSendToUser(game.getPlayer(), "/topic/ready", Result.ok("", game.getOwner()));
                }
            }
            //当前准备的是第二个玩家，校验当前准备的玩家是否为此房间的第二个玩家
            else if (game.getPlayer() != null && username.equals(game.getPlayer())) {
                if (game.isOwnerReady()) {
                    //房主已经准备，直接开始游戏
                    startGame(game);
                    return;
                }
                game.setPlayerReady(true);
                //推送准备消息给房主
                sender.convertAndSendToUser(game.getOwner(), "/topic/ready", Result.ok("", game.getPlayer()));
            }
        }
    }

    @Override
    public void startGame(Game game) {
        game.setPlaying(true);
        setPlayerRole(game);
        //推送游戏开始和谁是黑棋方的消息给 房间中的两个玩家
        sender.convertAndSendToUser(game.getOwner(), "/topic/start", Result.ok("", game.getBlackRole()));
        sender.convertAndSendToUser(game.getPlayer(), "/topic/start", Result.ok("", game.getBlackRole()));
        //修改房间状态为游戏中
        Room room = RoomManager.get(game.getOwner());
        room.setPlaying(true);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("owner", room.getOwner());
        map2.put("room", room);
        //推送此房间已开始游戏的消息给 大厅中的所有用户
        sender.convertAndSend("/topic/updateRoom", Result.ok("", map2));
    }

    /**
     *  设置玩家角色，围棋是黑白，象棋是红黑需要子类决定角色的设置
     * @param game
     */
    abstract public void setPlayerRole(Game game);
}
