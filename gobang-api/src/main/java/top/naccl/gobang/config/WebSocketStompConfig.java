package top.naccl.gobang.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * @Description: WebSocketStomp配置
 * @Author: Naccl
 * @Date: 2020-11-08
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

	@Autowired
	private MyWebSocketHandlerDecoratorFactory myWebSocketHandlerDecoratorFactory;

	/**
	 * 配置WebSocket进入点，开启SockJS，用于WebSocket握手连接
	 *
	 * @param registry
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
				.setHandshakeHandler(new MyHandshakeHandler())
				.setAllowedOrigins("*")
				.withSockJS();
	}

	/**
	 * 配置消息代理
	 *
	 * @param registry
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 配置客户端发送请求消息的一个或多个前缀，该前缀会筛选消息目标转发到 Controller 类中注解对应的方法里
		registry.setApplicationDestinationPrefixes("/send");
		// 设置一个或者多个代理前缀，在 Controller 类中的方法里面发生的消息，会首先转发到代理从而发送到对应广播或者队列中
		registry.enableSimpleBroker("/topic");
		// 服务端通知客户端的前缀，可以不设置，默认为user
		registry.setUserDestinationPrefix("/user");
	}

	/**
	 * 配置监听websocket的连接和断开连接
	 *
	 * @param registry
	 */
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.addDecoratorFactory(myWebSocketHandlerDecoratorFactory);
	}
}
