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
public class Game {
	private static int rows = 15;//行数
	private static int cols = 15;//列数

	private String owner;
	private String player;
	private boolean isPlaying = false;

	private boolean ownerReady = false;
	private boolean playerReady = false;
	private String blackRole;//黑方
	private String whiteRole;//白方
	private int[][] matrix = new int[rows][cols];//棋盘 0空 1黑棋 2白棋
	private Chess[] chessArray = new Chess[rows * cols];//按顺序记录棋子
	private int chessCount = 0;//已下棋子个数
	private boolean isBlackNow = (this.chessCount & 1) == 0;//当前棋子颜色
}
