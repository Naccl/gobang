package top.naccl.gobang.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
	// 队列
	public static final String TOPIC_QUEUE_100 = "queue_100";

	// 路由键
	public static final String TOPIC_ROUTINGKEY_100 = "topic.routing.100";

	// 交换机模式
	public static final String FANOUT_EXCHANGE = "fanout_exchange";
	public static final String DIRECT_EXCHANGE = "direct_exchange";
	public static final String TOPIC_EXCHANGE = "topic_exchange";
	public static final String HEADERS_EXCHANGE = "headers_exchange";

	// 生成100队列
	@Bean
	public Queue queue100() {
		return new Queue(TOPIC_QUEUE_100, true);
	}
	
	// topic模式的交换机
	@Bean
	public TopicExchange topicExchage(){
		return new TopicExchange(TOPIC_EXCHANGE, true, false);
	}

	// 绑定100队列和交换机
	@Bean
	public Binding topicBinding100() {
		return BindingBuilder.bind(queue100()).to(topicExchage()).with(TOPIC_ROUTINGKEY_100);
	}

	
	
}
