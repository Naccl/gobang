package top.naccl.gobang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import top.naccl.gobang.util.JwtUtils;

import java.security.Principal;
import java.util.Map;

/**
 * @Description: 握手期间权限校验
 * @Author: Naccl
 * @Date: 2020-11-09
 */
@Slf4j
public class MyHandshakeHandler extends DefaultHandshakeHandler {
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		ServletServerHttpRequest req = (ServletServerHttpRequest) request;
		String token = req.getServletRequest().getParameter("token");
		if (StringUtils.hasText(token)) {
			String username;
			try {
				username = JwtUtils.getTokenBody(token).getSubject();
				return new Principal() {
					@Override
					public String getName() {
						return username;
					}
				};
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
