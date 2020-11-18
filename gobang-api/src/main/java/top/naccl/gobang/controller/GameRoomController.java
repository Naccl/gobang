package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;

import java.security.Principal;

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

	/**
	 * 用户订阅此消息后，将 房间信息 推送给用户
	 *
	 * @return
	 */
	@SubscribeMapping("/game")
	public Result game(Principal principal) {
		String username = principal.getName();
		Game game = GameManager.getGameByUsername(username);
		return Result.ok("", game);
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
		//todo 如果两个都准备 就开始游戏
		if (owner.equals(username)) {
			//当前准备的玩家就是房主
			game.setOwnerReady(true);
			if (game.getPlayer() != null) {
				//存在第二个玩家，推送准备消息给对方
				sender.convertAndSendToUser(game.getPlayer(), "/topic/ready", Result.ok("", owner));
			}
		} else {
			//当前准备的是第二个玩家，校验当前准备的玩家是否为此房间的第二个玩家
			if (game.getPlayer() != null && username.equals(game.getPlayer())) {
				game.setPlayerReady(true);
				//推送准备消息给房主
				sender.convertAndSendToUser(owner, "/topic/ready", Result.ok("", game.getPlayer()));
			}
		}
	}
}
