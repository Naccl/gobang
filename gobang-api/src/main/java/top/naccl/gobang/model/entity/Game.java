package top.naccl.gobang.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: 游戏对局信息
 * @Author: Naccl
 * @Date: 2020-11-12
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
//todo 这个类应该再抽象一下，或者抽出一个父类。留给后面其他棋种的拓展性
public class Game {
	public static final int rows = 15;//行数
	public static final int cols = 15;//列数
	private int sameColorCount = 0;//记录连珠个数

	private String owner;
	private String player;
	private boolean isPlaying = false;

	private boolean ownerReady = false;
	private boolean playerReady = false;
	private String blackRole;//黑方
	private String whiteRole;//白方
	private boolean isWin = false;//是否决出胜负
	private String heqiUsername;//申请和棋方用户名
	private String undoUsername;//申请悔棋方用户名

	private int[][] matrix = new int[rows][cols];//棋盘 0空 1黑棋 2白棋
	private goBang[] chessArray = new goBang[rows * cols];//按顺序记录棋子
	private int chessCount = 0;//已下棋子个数
	private boolean isBlackNow = true;//当前棋子颜色

	public void init() {
		this.sameColorCount = 0;
		this.isPlaying = false;
		this.ownerReady = false;
		this.playerReady = false;
		this.blackRole = null;
		this.whiteRole = null;
		this.isWin = false;
		this.heqiUsername = null;
		this.undoUsername = null;
		this.matrix = new int[rows][cols];
		this.chessArray = new goBang[rows * cols];
		this.chessCount = 0;
		this.isBlackNow = true;
	}
}
