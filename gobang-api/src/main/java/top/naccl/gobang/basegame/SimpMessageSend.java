package top.naccl.gobang.basegame;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 17:06
 * @Description:
 */
public abstract class SimpMessageSend implements BeanFactoryAware {

    protected SimpMessageSendingOperations sender;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.sender = beanFactory.getBean(SimpMessageSendingOperations.class);
    }
}
