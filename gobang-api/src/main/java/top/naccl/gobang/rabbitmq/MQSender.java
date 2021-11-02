package top.naccl.gobang.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.gobang.config.MQConfig;
import top.naccl.gobang.model.message.MatchingMQEventMessage;
import top.naccl.gobang.util.JacksonUtils;

@Service
public class MQSender {
	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sent_100(MatchingMQEventMessage matchingMessage) {
		// 将消息体传入，并设置消息为不持久（服务器关闭，消息就没）
		Message message = MessageBuilder.withBody(JacksonUtils.writeValueAsBytes(matchingMessage)).setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT).build();
		rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, MQConfig.TOPIC_ROUTINGKEY_100, message);
	}
}
