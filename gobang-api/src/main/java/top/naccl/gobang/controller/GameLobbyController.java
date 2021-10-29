package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.gobang.manager.GameManager;
import top.naccl.gobang.manager.RoomManager;
import top.naccl.gobang.manager.SessionManager;
import top.naccl.gobang.model.entity.Game;
import top.naccl.gobang.model.entity.Result;
import top.naccl.gobang.model.entity.Room;
import top.naccl.gobang.service.GameLobbyService;

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
	private SimpMessageSendingOperations sender;

	@Autowired
	private GameLobbyService gameLobbyService;

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
		sender.convertAndSend("/topic/playerCount", Result.ok("", playerCount));
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
		Room room = RoomManager.getRoomByUsername(username);
		//todo 判断游戏是否进行中
		if (username.equals(room.getOwner())) {
			//退出的玩家是房主
			if (room.getPlayer() == null) {
				//房间中不存在第二个玩家，直接移除房间并通知大厅所有人
				RoomManager.remove(username);
				GameManager.remove(username);
				Map<String, Object> map = new HashMap<>();
				map.put("owner", username);
				//房主退出房间后回到大厅，房间数量可能显示不准确
				map.put("roomCount", RoomManager.count());
				sender.convertAndSend("/topic/removeRoom", Result.ok("", map));
			} else {
				//房间中存在第二个玩家，转让房主
				String player = room.getPlayer();
				Room newRoom = new Room();
				newRoom.setOwner(player);
				RoomManager.add(player, newRoom);
				RoomManager.remove(username);
				//通知大厅所有人更新房间信息
				Map<String, Object> map = new HashMap<>();
				map.put("owner", username);
				map.put("room", newRoom);
				sender.convertAndSend("/topic/updateRoom", Result.ok("", map));
				//游戏对局也做同样的处理
				Game newGame = new Game();
				newGame.setOwner(player);
				GameManager.add(player, newGame);
				GameManager.remove(username);
				//todo 告诉第二个玩家 房主已经退出
			}
		} else {
			//退出房间的是第二个玩家，直接退出
			room.setPlayer(null);
			Game game = GameManager.get(room.getOwner());
			game.setPlayer(null);
			game.setPlayerReady(false);
			Map<String, Object> map = new HashMap<>();
			map.put("owner", room.getOwner());
			map.put("room", room);
			//通知大厅所有人更新房间信息
			sender.convertAndSend("/topic/updateRoom", Result.ok("", map));
			//todo 告诉房主 第二个玩家已经退出
		}
	}

	// 加入匹配
	@MessageMapping("/matching")
	public void matching(Principal principal) {
		String username = principal.getName();
		gameLobbyService.joinMatching(username);
	}

	// 取消匹配
	@MessageMapping("/unMatching")
	public void unMatching(Principal principal) {
		String username = principal.getName();
		gameLobbyService.UnMatching(username);
		log.debug("取消匹配: {}", username);
	}
}
