package top.naccl.gobang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-11-01 16:31
 * @description: TODO
 */
@Slf4j
@Component
public class MyWebSocketHandlerDecorator extends WebSocketHandlerDecorator implements BeanFactoryAware {
    private static BeanFactory beanFactory;

    public MyWebSocketHandlerDecorator(WebSocketHandler handler) {
        super(handler);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
