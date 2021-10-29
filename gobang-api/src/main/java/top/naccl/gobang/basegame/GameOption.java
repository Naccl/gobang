package top.naccl.gobang.basegame;

import top.naccl.gobang.model.entity.Chess;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 17:18
 * @Description: 游戏中的操作
 */
public interface GameOption {

    /**
     * 玩家落子
     *
     * @param username 身份标识
     * @param chess     棋子坐标信息
     */
    void setChess(String username, Chess chess);

    /**
     * 申请悔棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void undo(String owner, String username);

    /**
     * 同意悔棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void agreeUndo(String owner, String username);

    /**
     * 拒绝悔棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void refuseUndo(String owner, String username);

    /**
     * 申请和棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void heqi(String owner, String username);

    /**
     * 同意和棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void agreeHeqi(String owner, String username);

    /**
     * 拒绝和棋
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void refuseHeqi(String owner, String username);

    /**
     * 认输
     *
     * @param owner     房主用户名
     * @param username 身份标识
     */
    void capitulate(String owner, String username);
}
