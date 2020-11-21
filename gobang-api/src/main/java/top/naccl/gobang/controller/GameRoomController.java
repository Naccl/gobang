package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.model.entity.Chess;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Description: 游戏房间
 * @Author: Naccl
 * @Date: 2020-11-12
 */
@Slf4j
@RestController
public class GameRoomController {
	@Autowired
	private SimpMessageSendingOperations sender;
	private static final Random random = new Random();
	//深搜判断胜负用到的八个搜索方向
	private static final int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
	private static final int[] dy = {1, 1, 0, -1, -1, -1, 0, 1};
	private static final int DEFAULT_DIRECTION = 8;

	/**
	 * 用户订阅此消息后，将 房间信息 推送给用户
	 *
	 * @param principal 身份标识
	 * @return
	 */
	@SubscribeMapping("/game")
	public Result game(Principal principal) {
		String username = principal.getName();
		Game game = GameManager.getGameByUsername(username);
		Map<String, Object> map = new HashMap<>();
		map.put("me", username);
		map.put("owner", game.getOwner());
		map.put("player", game.getPlayer());
		map.put("ownerReady", game.isOwnerReady());
		map.put("playerReady", game.isPlayerReady());
		return Result.ok("", map);
	}

	/**
	 * 玩家准备
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/ready")
	public void ready(String owner, Principal principal) {
		String username = principal.getName();
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

	/**
	 * 游戏开始处理方法
	 *
	 * @param game 游戏对局
	 */
	private void startGame(Game game) {
		game.setPlaying(true);
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
	 * 玩家落子
	 *
	 * @param principal 身份标识
	 * @param data      棋子坐标信息
	 */
	@MessageMapping("/setChess")
	public void setChess(Principal principal, Map data) {
		String username = principal.getName();
		String owner = (String) data.get("owner");
		int x = (int) data.get("x");
		int y = (int) data.get("y");
		Game game = GameManager.get(owner);
		//判断玩家落子是否合法
		if (judgeSetChess(game, username, x, y)) {
			game.getMatrix()[y][x] = game.isBlackNow() ? 1 : 2;
			game.getChessArray()[game.getChessCount()] = new Chess(x, y, game.isBlackNow());
			game.setChessCount(game.getChessCount() + 1);
			Map<String, Object> map = new HashMap<>();
			map.put("x", x);
			map.put("y", y);
			map.put("isBlack", game.isBlackNow());
			//推送落子消息给 房间中的两个玩家
			sender.convertAndSendToUser(game.getOwner(), "/topic/setChess", Result.ok("", map));
			sender.convertAndSendToUser(game.getPlayer(), "/topic/setChess", Result.ok("", map));
			//判断是否获胜
			isWin(x, y, DEFAULT_DIRECTION, game);
			if (game.isWin()) {
				String msg;
				if (game.isBlackNow()) {
					msg = "黑棋获胜！";
				} else {
					msg = "白棋获胜！";
				}
				//推送胜负消息给 房间中的两个玩家
				sender.convertAndSendToUser(game.getOwner(), "/topic/win", Result.ok("", msg));
				sender.convertAndSendToUser(game.getPlayer(), "/topic/win", Result.ok("", msg));
				//todo 处理对局信息、记录分数
				//初始化对局状态
				game.init();
				return;
			} else if (game.getChessCount() == Game.rows * Game.cols) {
				//如果棋盘已经下满了，但还没分出胜负，直接平局
				String msg = "平局！";
				sender.convertAndSendToUser(game.getOwner(), "/topic/win", Result.ok("", msg));
				sender.convertAndSendToUser(game.getPlayer(), "/topic/win", Result.ok("", msg));
				//todo 处理对局信息、记录分数
				//初始化对局状态
				game.init();
				return;
			}
			game.setBlackNow(!game.isBlackNow());
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

	/**
	 * 判断玩家落子是否合法
	 * 1.正在游戏中
	 * 2.现在轮到黑棋且发来落子消息的是黑棋方 或 现在轮到白棋且发来落子消息的是白棋方
	 * 3.落子位置不存在棋子
	 * 4.落子位置没有超出棋盘边界
	 *
	 * @param game     对局信息
	 * @param username 落子操作用户名
	 * @param x        对应二维数组中的行
	 * @param y        对应二维数组中的列
	 * @return
	 */
	private boolean judgeSetChess(Game game, String username, int x, int y) {
		return game.isPlaying()
				&& ((game.isBlackNow() && username.equals(game.getBlackRole())) || (!game.isBlackNow() && username.equals(game.getWhiteRole())))
				&& game.getMatrix()[y][x] == 0
				&& judgeBorder(x, y);
	}

	/**
	 * 深搜判断是否五连珠
	 *
	 * @param x    对应二维数组中的行
	 * @param y    对应二维数组中的列
	 * @param d    方向
	 * @param game 对局信息
	 */
	private void isWin(int x, int y, int d, Game game) {
		if (game.isWin()) {
			return;
		}
		//起始四个方向
		if (d == DEFAULT_DIRECTION) {
			for (int i = 0; i < 4; i++) {
				game.setSameColorCount(1);
				int nx1 = x + dx[i];
				int ny1 = y + dy[i];
				int nx2 = x + dx[i + 4];
				int ny2 = y + dy[i + 4];
				if (judgeBorder(nx1, ny1)) isWin(nx1, ny1, i, game);
				if (judgeBorder(nx2, ny2)) isWin(nx2, ny2, i + 4, game);
			}
		} else if ((game.isBlackNow() && game.getMatrix()[y][x] == 1) || (!game.isBlackNow() && game.getMatrix()[y][x] == 2)) {
			//当前方向下一个位置是否连珠
			game.setSameColorCount(game.getSameColorCount() + 1);
			int nx = x + dx[d];
			int ny = y + dy[d];
			if (judgeBorder(nx, ny)) isWin(nx, ny, d, game);
		}
		if (game.getSameColorCount() >= 5) {
			game.setWin(true);
		}
	}

	/**
	 * 申请和棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/heqi")
	public void heqi(String owner, Principal principal) {
		String username = principal.getName();
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

	/**
	 * 同意和棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/agreeHeqi")
	public void agreeHeqi(String owner, Principal principal) {
		String username = principal.getName();
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
			//todo 处理对局信息、记录分数
			//初始化对局状态
			game.init();
		}
	}

	/**
	 * 拒绝和棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/refuseHeqi")
	public void refuseHeqi(String owner, Principal principal) {
		String username = principal.getName();
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

	/**
	 * 认输
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/capitulate")
	public void capitulate(String owner, Principal principal) {
		String username = principal.getName();
		Game game = GameManager.get(owner);
		//1.正在游戏中
		//2.玩家在此对局中
		if (game.isPlaying()) {
			if (username.equals(game.getBlackRole())) {
				String msg = "黑棋认输，白棋获胜！";
				sender.convertAndSendToUser(game.getBlackRole(), "/topic/win", Result.ok("", msg));
				sender.convertAndSendToUser(game.getWhiteRole(), "/topic/win", Result.ok("", msg));
				//todo 处理对局信息、记录分数
				//初始化对局状态
				game.init();
			} else if (username.equals(game.getWhiteRole())) {
				String msg = "白棋认输，黑棋获胜！";
				sender.convertAndSendToUser(game.getBlackRole(), "/topic/win", Result.ok("", msg));
				sender.convertAndSendToUser(game.getWhiteRole(), "/topic/win", Result.ok("", msg));
				//todo 处理对局信息、记录分数
				//初始化对局状态
				game.init();
			}
		}
	}
}
