package top.naccl.gobang.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @program: Gobang
 * @author: ChenJin
 * @create: 2021-10-29 10:18
 * @description: TODO
 */
public class RabbitmqUtils {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqUtils.class);

    // 暂时不可用，rabbitTemplate为空
    public static int getMessageCount(String queue) {
        if (StringUtils.isEmpty(queue)) {
            log.error("getMessageCount 参数不能为空");
        }
        RabbitTemplate rabbitTemplate = new RabbitTemplate(); // null
        return Objects.requireNonNull(rabbitTemplate.execute(new ChannelCallback<AMQP.Queue.DeclareOk>() {
            public AMQP.Queue.DeclareOk doInRabbit(Channel channel) throws Exception {
                return channel.queueDeclarePassive(queue);
            }
        })).getMessageCount();
    }
}
