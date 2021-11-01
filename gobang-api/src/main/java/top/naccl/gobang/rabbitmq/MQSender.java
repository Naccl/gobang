package top.naccl.gobang.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.naccl.gobang.config.MQConfig;
import top.naccl.gobang.model.message.MatchingMQEventMessage;

@Service
public class MQSender {
	private static final Logger log = LoggerFactory.getLogger(MQSender.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sent_100(MatchingMQEventMessage matchingMessage) {
		rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, MQConfig.TOPIC_ROUTINGKEY_100, matchingMessage);
	}

}
