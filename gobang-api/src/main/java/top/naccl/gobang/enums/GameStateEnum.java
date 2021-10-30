package top.naccl.gobang.enums;

public enum GameStateEnum {
    PEACE("平局",0), // 和棋
    WINORLOSE("赢输",1), // 赢输
    ING("正在进行的棋局",2), // 正在进行的棋局
    FREE("空闲的棋局",3); // 空闲的棋局

    private final int state;

    private GameStateEnum(String stateName,int state) {
        this.state = state;
    }

    public int getType() {
        return this.state;
    }
}
