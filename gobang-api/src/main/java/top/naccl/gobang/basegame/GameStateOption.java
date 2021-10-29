package top.naccl.gobang.basegame;

import top.naccl.gobang.model.entity.Game;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 16:55
 * @Description: 游戏对局操作
 */
public interface GameStateOption {


    /**
     * 玩家准备
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void ready(String owner, String username);

    /**
     * 游戏开始处理方法
     *
     * @param game 游戏对局
     */
    void startGame(Game game);
}


