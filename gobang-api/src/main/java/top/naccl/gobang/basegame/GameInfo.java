package top.naccl.gobang.basegame;

import top.naccl.gobang.enums.GameStateEnum;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.goBang;

import java.util.Map;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 15:52
 * @Description: 获取游戏对局信息方法
 */
public interface GameInfo {

    /**
     * 用户订阅此消息后，将 房间信息 推送给用户
     *
     * @param username 身份标识
     */
    Map<String,Object> game(String username);

    /**
     * 深搜判断是否五连珠
     *
     * 		 x    对应二维数组中的行
     * 		 y    对应二维数组中的列
     * 		 d    方向
     * @param op 存储对应的操作信息，这里存储的是判断连珠方向
     * @param game 对局信息
     */
    void isWin(goBang goBang, Game game, Object... op);

    /**
     * 统计对局信息
     * @param winName 获胜用户名称
     * @param loseName 失败用户名称
     * @param state 对局状态
     */
    void gameStatistics(String winName, String loseName, GameStateEnum state);
}
