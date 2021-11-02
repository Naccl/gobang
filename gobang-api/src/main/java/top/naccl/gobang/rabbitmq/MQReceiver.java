package top.naccl.gobang.rabbitmq;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.naccl.gobang.config.MQConfig;
import top.naccl.gobang.model.message.MatchingMQEventMessage;
import top.naccl.gobang.service.GameLobbyService;
import top.naccl.gobang.service.RedisService;
import top.naccl.gobang.util.JacksonUtils;

import java.io.IOException;

@Service
public class MQReceiver {
	private static final Logger log = LoggerFactory.getLogger(MQReceiver.class);

	@Autowired
	private GameLobbyService gameLobbyService;

	@Autowired
	private RedisService redisService;

//	private ReentrantLock lock = new ReentrantLock();
	private  String waitName = null;
	// 为了消息的可靠性，设置为手动应答
	@RabbitListener(queues = MQConfig.TOPIC_QUEUE_100, ackMode = "MANUAL")
	public void receive(byte[] message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
		MatchingMQEventMessage matchingMessage = JacksonUtils.readValue(message, MatchingMQEventMessage.class);
		String username = matchingMessage.getUsername();
		log.info("receive message: {}", username);
		if (!redisService.get(username).equals("0")) {
			// 如果用户没有取消匹配
			// 如果匹配不到，取消了，就将第一次进入队列的用户置空并且消息不处理直接ack
			if (!StringUtils.isEmpty(waitName) && redisService.get(waitName).equals("0")) {
				waitName = null;
			} else if (StringUtils.isEmpty(waitName)) {
				// 匹配到第一个玩家
				// 校验一下，防止玩家在该处取消匹配
				if(!(redisService.del(username).equals(0))) {
					waitName = username;
				}
			} else {
				// 匹配到第二个玩家
				// 直接删除第一个玩家信息，如果返回不为0 则表示有值
				if (!(redisService.del(waitName).equals(0)) && redisService.del(username).equals(0)) {
					gameLobbyService.createRoom(waitName);
					gameLobbyService.enterRoom(waitName, username);
					waitName = null;
				}
			}
		}
		try {
			// 设置为不批量应答
			channel.basicAck(deliveryTag, false);
			//重新进入队列
//			channel.basicNack(deliveryTag, false, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
