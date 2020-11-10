package top.naccl.gobang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;
import top.naccl.gobang.manager.WebSocketSessionManager;

import java.security.Principal;
import java.util.Map;

/**
 * @Description: 监听websocket通道的消息
 * @Author: Naccl
 * @Date: 2020-11-08
 */
@Slf4j
@RestController
public class WebSocketController {
	@Autowired
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@GetMapping("/sendAllUser")
	public void sendAllUser() {
		log.info("向所有用户发送消息");
		simpMessageSendingOperations.convertAndSend("/topic/sendAllUser", "推送所有用户的消息1");
	}

	@GetMapping("/sendOneUser/{username}")
	public void sendOneUser(@PathVariable String username) {
		log.info("向单个用户推送消息");
		WebSocketSession session = WebSocketSessionManager.get(username);
		if (session != null) {
			simpMessageSendingOperations.convertAndSendToUser(username, "/topic/sendOneUser", "向单个用户推送消息");
		}
	}

	@MessageMapping("/userSendServer")
	public void sendServer(String msg, Principal principal) {
		log.info("{} 向服务端发送消息 {}", principal.getName(), msg);
	}

	@MessageMapping("userSendAllUser")
	public void userSendAllUser(String msg) {
		log.info("客户端向所有客户端发送消息 {}", msg);
		simpMessageSendingOperations.convertAndSend("/topic/userSendAllUser", "客户端向所有客户端发送消息 : " + msg);
	}

	@MessageMapping("/userSendOneUser")
	public void userSendOneUser(Map map) {
		String username = (String) map.get("username");
		String msg = (String) map.get("msg");
		log.info("客户端向单点客户端发送消息 {}", msg);
		WebSocketSession session = WebSocketSessionManager.get(username);
		if (session != null) {
			simpMessageSendingOperations.convertAndSendToUser(username, "/topic/userSendOneUser", "客户端向单点客户端发送消息 : " + msg);
		}
	}
}
