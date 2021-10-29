package top.naccl.gobang.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import top.naccl.gobang.rabbitmq.MQReceiver;

import java.util.Objects;

/**
 * @program: gobang
 * @author: ChenJin
 * @create: 2021-10-29 10:18
 * @description: TODO
 */
public class RabbitmqUtils {
    private static Logger log = LoggerFactory.getLogger(RabbitmqUtils.class);

    public static int getMessageCount(String queue) {
        if (StringUtils.isEmpty(queue)) {
            log.error("getMessageCount 参数不能为空");
        }
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        return Objects.requireNonNull(rabbitTemplate.execute(new ChannelCallback<AMQP.Queue.DeclareOk>() {
            public AMQP.Queue.DeclareOk doInRabbit(Channel channel) throws Exception {
                return channel.queueDeclarePassive(queue);
            }
        })).getMessageCount();
    }
}
