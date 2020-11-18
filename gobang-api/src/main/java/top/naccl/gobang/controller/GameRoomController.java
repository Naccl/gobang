package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
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

	/**
	 * 用户订阅此消息后，将 房间信息 推送给用户
	 *
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
	 * @param principal 身份标识
	 */
	@MessageMapping("/ready")
	public void ready(String owner, Principal principal) {
		String username = principal.getName();
		Game game = GameManager.get(owner);
		if (game.getOwner().equals(username)) {
			//当前准备的玩家是房主
			if (game.isPlayerReady()) {
				//对手已经准备，直接开始游戏
				startGame(game);
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
			}
			game.setPlayerReady(true);
			//推送准备消息给房主
			sender.convertAndSendToUser(game.getOwner(), "/topic/ready", Result.ok("", game.getPlayer()));
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
		Map<String, Object> map = new HashMap<>();
		map.put("blackRole", game.getBlackRole());
		map.put("whiteRole", game.getWhiteRole());
		//推送游戏开始和黑白棋方的消息给 房间中的两个玩家
		sender.convertAndSendToUser(game.getOwner(), "/topic/start", map);
		sender.convertAndSendToUser(game.getPlayer(), "/topic/start", map);
		//修改房间状态为游戏中
		Room room = RoomManager.get(game.getOwner());
		room.setPlaying(true);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("owner", room.getOwner());
		map2.put("room", room);
		//推送此房间已开始游戏的消息给 大厅中的所有用户
		sender.convertAndSend("/topic/updateRoom", map2);
	}
}
