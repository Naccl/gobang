package top.naccl.gobang.enums;

/**
 * @Auther: wAnG
 * @Date: 2021/10/30 19:29
 * @Description: 不同游戏类型胜负对应的积分
 */
public enum GameScoreEnum {

    Gobang("围棋",3);

    private final int score;

    private GameScoreEnum(String gameName,int score){
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
