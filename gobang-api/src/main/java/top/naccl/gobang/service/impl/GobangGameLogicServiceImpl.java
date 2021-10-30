package top.naccl.gobang.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.gobang.basegame.ChessGameOption;
import top.naccl.gobang.enums.GameScoreEnum;
import top.naccl.gobang.enums.GameStateEnum;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.mapper.ScoreMapper;
import top.naccl.gobang.model.entity.Chess;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Gobang;
import top.naccl.gobang.model.entity.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 17:00
 * @Description:
 */
@Slf4j
@Service
public class GobangGameLogicServiceImpl extends ChessGameOption {

    @Autowired
    private ScoreMapper scoreMapper;

    private static final int SCORE = GameScoreEnum.Gobang.getScore();

    private static final Random random = new Random();
    //深搜判断胜负用到的八个搜索方向
    private static final int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] dy = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final int DEFAULT_DIRECTION = 8;

    @Override
    public void setPlayerRole(Game game) {
        //随机产生黑白棋方
        if (random.nextBoolean()) {
            game.setBlackRole(game.getOwner());
            game.setWhiteRole(game.getPlayer());
        } else {
            game.setBlackRole(game.getPlayer());
            game.setWhiteRole(game.getOwner());
        }
        //推送游戏开始和谁是黑棋方的消息给 房间中的两个玩家
        sender.convertAndSendToUser(game.getOwner(), "/topic/start", Result.ok("", game.getBlackRole()));
        sender.convertAndSendToUser(game.getPlayer(), "/topic/start", Result.ok("", game.getBlackRole()));
    }

    @Override
    public void isWin(Gobang gobang, Game game, Object... op) {
        int direction = (int)op[0];
        int x = gobang.getX();
        int y = gobang.getY();
        if (game.isWin()) {
            return;
        }
        //起始四个方向
        if (direction == DEFAULT_DIRECTION) {
            for (int i = 0; i < 4; i++) {
                game.setSameColorCount(1);
                int nx1 = x + dx[i];
                int ny1 = y + dy[i];
                int nx2 = x + dx[i + 4];
                int ny2 = y + dy[i + 4];
                Gobang gobang1 = buildGobang(nx1, ny1);
                Gobang gobang2 = buildGobang(nx2, ny2);
                if (judgeBorder(nx1, ny1)) isWin(gobang1,game, i);
                if (judgeBorder(nx2, ny2)) isWin(gobang2,game, i + 4);
            }
        } else if ((game.isBlackNow() && game.getMatrix()[y][x] == 1) || (!game.isBlackNow() && game.getMatrix()[y][x] == 2)) {
            //当前方向下一个位置是否连珠
            game.setSameColorCount(game.getSameColorCount() + 1);
            int nx = x + dx[direction];
            int ny = y + dy[direction];
            if (judgeBorder(nx, ny)) isWin(buildGobang(nx,ny), game,direction);
        }
        if (game.getSameColorCount() >= 5) {
            game.setWin(true);
        }
    }

    @Override
    public void gameStatistics(String winName, String loseName, GameStateEnum state) {
        switch (state) {
            case PEACE:
                // todo 和局
                break;
            case WINORLOSE:
                // 有一方赢
                scoreMapper.updataScore(SCORE, winName);
                scoreMapper.updataScore(-SCORE, loseName);
                break;
        }
    }

    /**
     * 判断棋子是否在棋盘边界内
     *
     * @param x 对应二维数组中的行
     * @param y 对应二维数组中的列
     * @return
     */
    private boolean judgeBorder(int x, int y) {
        if (x < 0 || y < 0 || x >= Game.cols || y >= Game.rows) return false;
        return true;
    }

    private Gobang buildGobang(int x, int y){
        Gobang gobang = new Gobang();
        gobang.setX(x);
        gobang.setY(y);
        return gobang;
    }

    @Override
    public void setChess(String username, Chess chess) {
        /*
         * 		todo 手动把Map映射进入goBang,后期前端应该直接映射进参数
         * 		public void setChess(Principal principal, Gobang Gobang)
         */
        Gobang gobang = (Gobang) chess;
        int x = gobang.getX();
        int y = gobang.getY();
        String owner = gobang.getOwner();
        Game game = GameManager.get(owner);
        gobang.setUserName(username);
        //判断玩家落子是否合法
        if (judgeSetChess(game,gobang)) {
            game.getMatrix()[y][x] = game.isBlackNow() ? 1 : 2;
            // todo 这部分逻辑可以使用Arraylist开辟rows*cols大小进行add
            game.getChessArray()[game.getChessCount()] = gobang;
            game.setChessCount(game.getChessCount() + 1);
            // todo 返回前端的数据暂未修改，先不进行改动。后期应该改为goBang
            Map<String, Object> map = new HashMap<>();
            map.put("x", x);
            map.put("y", y);
            map.put("isBlack", game.isBlackNow());
            //推送落子消息给 房间中的两个玩家
            sender.convertAndSendToUser(game.getOwner(), "/topic/setChess", Result.ok("", map));
            sender.convertAndSendToUser(game.getPlayer(), "/topic/setChess", Result.ok("", map));
            //判断是否获胜
            isWin(gobang, game, DEFAULT_DIRECTION);
            String winName = null;
            String loseName = null;
            if (game.isWin()) {
                String msg;
                if (game.isBlackNow()) {
                    msg = "黑棋获胜！";
                    winName = game.getBlackRole();
                    loseName = game.getWhiteRole();
                } else {
                    msg = "白棋获胜！";
                    winName = game.getWhiteRole();
                    loseName = game.getBlackRole();
                }
                //推送胜负消息给 房间中的两个玩家
                sender.convertAndSendToUser(game.getOwner(), "/topic/win", Result.ok("", msg));
                sender.convertAndSendToUser(game.getPlayer(), "/topic/win", Result.ok("", msg));
                // 处理对局信息、记录分数
                gameStatistics(winName, loseName, GameStateEnum.WINORLOSE);
                //初始化对局状态
                game.init();
                return;
            } else if (game.getChessCount() == Game.rows * Game.cols) {
                //如果棋盘已经下满了，但还没分出胜负，直接平局
                String msg = "平局！";
                sender.convertAndSendToUser(game.getOwner(), "/topic/win", Result.ok("", msg));
                sender.convertAndSendToUser(game.getPlayer(), "/topic/win", Result.ok("", msg));
                // 处理对局信息、记录分数
                gameStatistics(game.getOwner(), game.getPlayer(), GameStateEnum.PEACE);
                //初始化对局状态
                game.init();
                return;
            }
            game.setBlackNow(!game.isBlackNow());
        }
    }

    /**
     * 判断玩家落子是否合法
     * 1.正在游戏中
     * 2.现在轮到黑棋且发来落子消息的是黑棋方 或 现在轮到白棋且发来落子消息的是白棋方
     * 3.落子位置不存在棋子
     * 4.落子位置没有超出棋盘边界
     *
     * @param game     对局信息
     *  	  username 落子操作用户名
     *  	  x        对应二维数组中的行
     * 	 	  y        对应二维数组中的列
     */
    private boolean judgeSetChess(Game game, Gobang gobang) {
        int x = gobang.getX();
        int y = gobang.getY();
        return game.isPlaying()
                && ((game.isBlackNow() && gobang.getUserName().equals(game.getBlackRole())) || (!game.isBlackNow() && gobang.getUserName().equals(game.getWhiteRole())))
                && game.getMatrix()[y][x] == 0
                && judgeBorder(x, y);
    }

    @Override
    public void undo(String owner, String username) {
        Game game = GameManager.get(owner);
        //正在游戏中
        if (game.isPlaying()) {
            //玩家在此对局中
            if (username.equals(game.getOwner())) {
                game.setUndoUsername(username);
                //向第二玩家发送请求悔棋消息
                sender.convertAndSendToUser(game.getPlayer(), "/topic/undo", Result.ok(""));
            } else if (username.equals(game.getPlayer())) {
                game.setUndoUsername(username);
                //向房主发送请求悔棋消息
                sender.convertAndSendToUser(game.getOwner(), "/topic/undo", Result.ok(""));
            }
        }
    }

    @Override
    public void agreeUndo(String owner, String username) {
        Game game = GameManager.get(owner);
        //1.正在游戏中
        //2.玩家在此对局中
        //3.只能同意对方的悔棋请求
        if (game.isPlaying() &&
                ((username.equals(game.getOwner()) && game.getPlayer().equals(game.getUndoUsername()))
                        || (username.equals(game.getPlayer()) && game.getOwner().equals(game.getUndoUsername())))) {
            //需要回退的棋子数量
            int count = 0;
            //现在轮到黑棋
            if (game.isBlackNow()) {
                //悔棋者是黑棋 回退两个棋子
                if (game.getUndoUsername().equals(game.getBlackRole())) {
                    count = 2;
                }
                //悔棋者是白棋 回退一个棋子
                else if (game.getUndoUsername().equals(game.getWhiteRole())) {
                    count = 1;
                }
            }
            //现在轮到白棋
            else {
                //悔棋者是黑棋 回退一个棋子
                if (game.getUndoUsername().equals(game.getBlackRole())) {
                    count = 1;
                }
                //悔棋者是白棋 回退两个棋子
                else if (game.getUndoUsername().equals(game.getWhiteRole())) {
                    count = 2;
                }
            }
            //执行悔棋操作
            int[][] matrix = game.getMatrix();
            Chess[] chessArray = game.getChessArray();
            int chessCount = game.getChessCount();
            while (count-- > 0 && chessCount > 0) {
                Chess chess = chessArray[--chessCount];
                matrix[chess.getX()][chess.getY()] = 0;
                chessArray[chessCount] = null;
                game.setChessCount(chessCount);
                game.setBlackNow(!game.isBlackNow());
            }
            //向两个玩家发送 悔棋后棋盘状态和接下来是谁的回合
            Map<String, Object> map = new HashMap<>();
            map.put("matrix", matrix);
            //现在轮到谁的回合
            String now = game.isBlackNow() ? game.getBlackRole() : game.getWhiteRole();
            map.put("now", now);
            //最后一个棋子的坐标
            if (chessCount > 0) {
                Chess lastChess = chessArray[chessCount - 1];
                map.put("lastX", lastChess.getX());
                map.put("lastY", lastChess.getY());
            }
            sender.convertAndSendToUser(game.getOwner(), "/topic/agreeUndo", Result.ok("", map));
            sender.convertAndSendToUser(game.getPlayer(), "/topic/agreeUndo", Result.ok("", map));
        }
    }

    @Override
    public void refuseUndo(String owner, String username) {
        Game game = GameManager.get(owner);
        //1.正在游戏中
        //2.玩家在此对局中
        //3.只能拒绝对方的悔棋请求
        if (game.isPlaying() &&
                ((username.equals(game.getOwner()) && game.getPlayer().equals(game.getUndoUsername()))
                        || (username.equals(game.getPlayer()) && game.getOwner().equals(game.getUndoUsername())))) {
            //发送拒绝悔棋消息给申请悔棋者
            sender.convertAndSendToUser(game.getUndoUsername(), "/topic/refuseUndo", Result.ok("", "对方拒绝悔棋"));
            game.setUndoUsername(null);
        }
    }
}
