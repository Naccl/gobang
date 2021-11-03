package top.naccl.gobang.rabbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class MQReceiver {
	@Autowired
	private GameLobbyService gameLobbyService;
	@Autowired
	private RedisService redisService;

	//正在等待匹配的用户
	private String waitName = null;

	// 为了消息的可靠性，设置为手动应答
	@RabbitListener(queues = MQConfig.TOPIC_QUEUE_100, ackMode = "MANUAL")
	public void receive(byte[] message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
		MatchingMQEventMessage matchingMessage = JacksonUtils.readValue(message, MatchingMQEventMessage.class);
		String username = matchingMessage.getUsername();
		log.info("receive message: {}", username);

		if ("1".equals(redisService.get(username))) {
			//当前用户没有取消匹配
			if (!StringUtils.isEmpty(waitName) && "1".equals(redisService.get(waitName)) && !waitName.equals(username)) {
				//已有一个用户正在等待匹配，且他没有取消匹配，并且不能是自己
				if (!(redisService.del(username) == 0) && !(redisService.del(waitName) == 0)) {
					//匹配成功
					//ack这条消息
				} else {
					//至少有一个牛逼用户取消了匹配，这时候怎么办呢
					log.warn("An user cancel match, username: {}, waitName: {}", username, waitName);
					//直接让他们匹配成功
				}
				gameLobbyService.createRoom(waitName);
				gameLobbyService.enterRoom(waitName, username);
				waitName = null;
				//ack这条消息
			} else {
				//没有用户在等待匹配，将当前用户加入等待
				//或上一个用户已经取消匹配，当前用户进入等待
				waitName = username;
				//ack这条消息
			}
		} else {
			//该用户已经取消匹配，ack这条消息
		}
		try {
			// 设置为不批量应答
			channel.basicAck(deliveryTag, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
