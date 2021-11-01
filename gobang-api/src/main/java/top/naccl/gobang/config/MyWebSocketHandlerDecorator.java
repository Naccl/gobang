package top.naccl.gobang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-11-01 16:31
 * @description: TODO
 */
@Slf4j
public class MyWebSocketHandlerDecorator extends WebSocketHandlerDecorator {
    protected final BeanFactory beanFactory;

    public MyWebSocketHandlerDecorator(WebSocketHandler handler,BeanFactory beanFactory) {
        super(handler);
        this.beanFactory = beanFactory;
    }

}
