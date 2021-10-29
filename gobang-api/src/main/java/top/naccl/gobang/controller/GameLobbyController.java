package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.service.GameLobbyService;

import java.security.Principal;
import java.util.Map;

/**
 * @Description: 游戏大厅
 * @Author: Naccl
 * @Date: 2020-11-11
 */
@Slf4j
@RestController
public class GameLobbyController {

	@Autowired
	private GameLobbyService gameLobbyService;

	/**
	 * 用户订阅此消息后，将 当前在线人数、房间数、房间列表 推送给用户
	 *
	 * @return
	 */
	@SubscribeMapping("/home")
	public Result getRoomList() {
		// todo 后期应该修改模型，不应该使用Map存储
		Map<String,Object> roomList = gameLobbyService.getRoomList();
		return Result.ok("", roomList);
	}

	/**
	 * 定时推送当前在线人数给 所有在游戏大厅的用户
	 */
	@Scheduled(fixedRate = 10000)
	public void notifyAllOnlineCount() {
		gameLobbyService.notifyAllOnlineCount();
	}

	/**
	 * 用户创建房间
	 *
	 * @param principal 身份标识
	 */
	@MessageMapping("/createRoom")
	public void createRoom(Principal principal) {
		String username = principal.getName();
		gameLobbyService.createRoom(username);
	}

	/**
	 * 用户进入房间
	 *
	 * @param owner     即将进入的房间的房主用户名
	 * @param principal 身份标识
	 */
	@MessageMapping("/enterRoom")
	public void enterRoom(String owner, Principal principal) {
		String username = principal.getName();
		gameLobbyService.enterRoom(owner, username);
	}

	/**
	 * 用户退出房间
	 *
	 * @param principal 身份标识
	 */
	@MessageMapping("/exitRoom")
	public void exitRoom(Principal principal) {
		String username = principal.getName();
		gameLobbyService.exitRoom(username);
	}

	// 加入匹配
	@MessageMapping("/matching")
	public void matching(Principal principal) {
		String username = principal.getName();
		gameLobbyService.joinMatching(username);
	}

}
