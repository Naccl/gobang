package top.naccl.gobang.rabbitmq;

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
		rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, MQConfig.TOPIC_ROUTINGKEY_100, JacksonUtils.writeValueAsString(matchingMessage));
	}
}
