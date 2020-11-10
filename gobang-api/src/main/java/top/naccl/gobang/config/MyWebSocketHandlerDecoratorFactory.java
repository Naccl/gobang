package top.naccl.gobang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import top.naccl.gobang.manager.WebSocketSessionManager;

import java.security.Principal;

/**
 * @Description: 监听websocket的连接和断开连接
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Slf4j
public class MyWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
	@Override
	public WebSocketHandler decorate(WebSocketHandler handler) {
		return new WebSocketHandlerDecorator(handler) {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				log.info("一个客户端成功连接: sessionId = {}", session.getId());
				Principal principal = session.getPrincipal();
				if (principal != null) {
					//握手时身份校验成功，将websocket session存入管理器
					WebSocketSessionManager.add(principal.getName(), session);
				} else {
					//握手时身份校验失败，关闭session
					session.close();
				}
				super.afterConnectionEstablished(session);
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
				log.info("一个客户端退出连接: sessionId = {}, closeStatus = {}", session.getId(), closeStatus);
				Principal principal = session.getPrincipal();
				if (principal != null) {
					//将websocket移出session管理器
					WebSocketSessionManager.remove(principal.getName());
				}
				super.afterConnectionClosed(session, closeStatus);
			}
		};
	}
}
