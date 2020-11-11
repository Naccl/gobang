package top.naccl.gobang.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: websocket session管理器
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Slf4j
public class SessionManager {
	private static ConcurrentHashMap<String, WebSocketSession> manager = new ConcurrentHashMap<>();

	public static void add(String key, WebSocketSession webSocketSession) {
		log.info("添加webSocket连接: username = {}", key);
		manager.put(key, webSocketSession);
	}

	public static void remove(String key) {
		log.info("移除webSocket连接: username = {}", key);
		manager.remove(key);
	}

	public static WebSocketSession get(String key) {
		log.info("查询webSocket连接: username = {}", key);
		return manager.get(key);
	}

	public static int count() {
		return manager.size();
	}
}
