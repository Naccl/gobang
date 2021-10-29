package top.naccl.gobang.basegame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

/**
 * @Auther: wAnG
 * @Date: 2021/10/29 17:06
 * @Description:
 */
public abstract class SimpMessageSend {

    @Autowired
    protected SimpMessageSendingOperations sender;

}
