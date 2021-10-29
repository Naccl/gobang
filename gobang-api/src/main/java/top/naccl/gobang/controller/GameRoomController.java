package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.goBang;
import top.naccl.gobang.service.impl.GoBangGameLogicServiceImpl;

import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.model.entity.*;
import top.naccl.gobang.service.UserService;
import java.security.Principal;
import java.util.Map;

/**
 * @Description: 游戏房间
 * @Author: Naccl
 * @Date: 2020-11-12
 */
@Slf4j
@RestController
public class GameRoomController {

	@Autowired
	private GoBangGameLogicServiceImpl goBangGameLogicService;

	/**
	 * 用户订阅此消息后，将 房间信息 推送给用户
	 *
	 * @param principal 身份标识
	 * @return
	 */
	@SubscribeMapping("/game")
	public Result game(Principal principal) {
		String username = principal.getName();
		Map<String, Object> gameInfo = goBangGameLogicService.game(username);
		return Result.ok("", gameInfo);
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
		goBangGameLogicService.ready(owner,username);
	}

	/**
	 * 游戏开始处理方法
	 *
	 * @param game 游戏对局
	 */
	private void startGame(Game game) {
		goBangGameLogicService.startGame(game);
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
		/*
		 * 		todo 手动把Map映射进入goBang,后期前端应该直接映射进参数
		 * 		public void setChess(Principal principal, goBang goBang)
		 */
		goBang goBang = new goBang();
		goBang.setOwner((String) data.get("owner"));
		goBang.setX((Integer) data.get("x"));
		goBang.setY((Integer) data.get("y"));
		goBangGameLogicService.setChess(username,goBang);
	}




	/**
	 * 申请悔棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/undo")
	public void undo(String owner, Principal principal) {
		String username = principal.getName();
		goBangGameLogicService.undo(owner,username);
	}

	/**
	 * 同意悔棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/agreeUndo")
	public void agreeUndo(String owner, Principal principal) {
		String username = principal.getName();
		goBangGameLogicService.agreeUndo(owner,username);
	}

	/**
	 * 拒绝悔棋
	 *
	 * @param owner     房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/refuseUndo")
	public void refuseUndo(String owner, Principal principal) {
		String username = principal.getName();
		goBangGameLogicService.refuseUndo(owner,username);
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
		goBangGameLogicService.heqi(owner,username);
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
		goBangGameLogicService.agreeHeqi(owner,username);
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
		goBangGameLogicService.refuseHeqi(owner,username);
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
		goBangGameLogicService.capitulate(owner,username);
	}
}
