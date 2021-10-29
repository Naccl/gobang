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
import top.naccl.gobang.service.GameLobbyService;
import top.naccl.gobang.util.RabbitmqUtils;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MQReceiver {
	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	@Autowired
	private GameLobbyService gameLobbyService;

	private ReentrantLock lock = new ReentrantLock();
	private  String waitName = null;
	@RabbitListener(queues = MQConfig.TOPIC_QUEUE_100, ackMode = "MANUAL")
	public void receive(String username, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws InterruptedException, IOException {
		log.info("receive message: {}", username);
		try {
			lock.lock();
			if (StringUtils.isEmpty(waitName)) {
				waitName = username;
			} else {
				// RabbitMQ的ack机制中，第二个参数返回true，表示需要将这条消息投递给其他的消费者重新消费
				gameLobbyService.createRoom(waitName);
				gameLobbyService.enterRoom(waitName, username);
				channel.basicAck(deliveryTag, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
