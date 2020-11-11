package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.manager.SessionManager;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;

import java.security.Principal;
import java.util.HashMap;
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
	private SimpMessageSendingOperations simpMessageSendingOperations;

	/**
	 * 用户订阅此消息后，将 当前在线人数、房间数、房间列表 推送给用户
	 *
	 * @return
	 */
	@SubscribeMapping("/home")
	public Result getRoomList() {
		Map<String, Object> map = new HashMap<>();
		map.put("playerCount", SessionManager.count());
		map.put("roomCount", RoomManager.count());
		map.put("roomList", RoomManager.getRoomList());
		return Result.ok("", map);
	}


	/**
	 * 定时推送当前在线人数给 所有在游戏大厅的用户
	 */
	@Scheduled(fixedRate = 10000)
	public void notifyAllOnlineCount() {
		int playerCount = SessionManager.count();
		simpMessageSendingOperations.convertAndSend("/topic/playerCount", Result.ok("", playerCount));
	}

	/**
	 * 用户创建房间
	 *
	 * @param principal 身份标识
	 */
	@MessageMapping("/createRoom")
	public void createRoom(Principal principal) {
		String username = principal.getName();
		if (RoomManager.isExistUser(username)) {
			//此用户已经创建或进入了一个房间，不可创建其它房间
			simpMessageSendingOperations.convertAndSendToUser(username, "/topic/createRoom", Result.refuse("已经进入了一个房间，请先退出房间！"));
			return;
		}
		Room room = new Room();
		room.setOwner(username);
		room.setIsPlaying(false);
		RoomManager.add(username, room);
		//推送此消息给 所有在游戏大厅的用户
		simpMessageSendingOperations.convertAndSend("/topic/createRoom", Result.ok("", room));
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
		if (RoomManager.isExistUser(username)) {
			//此用户已经创建或进入了一个房间，不可进入其它房间
			simpMessageSendingOperations.convertAndSendToUser(username, "/topic/createRoom", Result.refuse("已经进入了一个房间，请先退出房间！"));
			return;
		}
		Room room = RoomManager.get(owner);
		room.setPlayer(username);
		//推送此消息给 所有在游戏大厅的用户
		simpMessageSendingOperations.convertAndSend("/topic/enterRoom", Result.ok("", room));
	}
}
