package top.naccl.gobang.enums;

public enum GameStateEnum {
    PEACE(0), // 和棋
    WINORLOSE(1), // 赢输
    ING(2), // 正在进行的棋局
    FREE(3); // 空闲的棋局

    private int state;

    GameStateEnum(int state) {
        this.state = state;
    }

    public int getType() {
        return this.state;
    }
}
