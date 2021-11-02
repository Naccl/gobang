package top.naccl.gobang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisPoolConfig {
	@Value("${spring.redis.host}")
	private String host;
	@Value("${spring.redis.port}")
	private int port;
	@Value("${spring.redis.password}")
	private String password;
	@Value("${spring.redis.database}")
	private int database;
	@Value("${spring.redis.timeout}")
	private int timeout;//秒
	@Value("${spring.redis.jedis.pool.max-idle}")
	private int poolMaxIdle;
	@Value("${spring.redis.jedis.pool.max-total}")
	private int poolMaxTotal;
	@Value("${spring.redis.jedis.pool.max-wait}")
	private int poolMaxWait;//秒

	@Bean
	public JedisPool JedisPoolFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(poolMaxIdle);
		poolConfig.setMaxTotal(poolMaxTotal);
		poolConfig.setMaxWaitMillis(poolMaxWait * 1000L);
		return new JedisPool(poolConfig, host, port, timeout * 1000, password, database);
	}
}
