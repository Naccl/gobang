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
import top.naccl.gobang.redis.RedisService;
import top.naccl.gobang.service.GameLobbyService;

import java.io.IOException;

@Service
public class MQReceiver {
	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

	@Autowired
	private GameLobbyService gameLobbyService;

	@Autowired
	private RedisService redisService;

//	private ReentrantLock lock = new ReentrantLock();
	private  String waitName = null;
	@RabbitListener(queues = MQConfig.TOPIC_QUEUE_100, ackMode = "MANUAL")
	public void receive(String username, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
		log.info("receive message: {}", username);
		if (!redisService.get(username).equals("0")) {
			//如果用户没有取消匹配
			// 如果匹配不到，取消了，就将第一次进入队列的用户置空并且消息不处理直接ack
			if (!StringUtils.isEmpty(waitName) && redisService.get(waitName).equals("0")) {
				waitName = null;
			} else if (StringUtils.isEmpty(waitName)) {
				waitName = username;
			} else {
				// RabbitMQ的ack机制中，第二个参数返回true，表示需要将这条消息投递给其他的消费者重新消费
				gameLobbyService.createRoom(waitName);
				gameLobbyService.enterRoom(waitName, username);
				redisService.del(waitName);
				redisService.del(username);
				waitName = null;
			}
		}
		try {
			channel.basicAck(deliveryTag, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
