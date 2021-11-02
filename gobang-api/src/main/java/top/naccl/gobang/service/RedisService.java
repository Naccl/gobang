package top.naccl.gobang.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RedisService implements InitializingBean {
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

	private JedisPool jedisPool;

	private JedisPool getJedisPool() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(poolMaxIdle);
		poolConfig.setMaxTotal(poolMaxTotal);
		poolConfig.setMaxWaitMillis(poolMaxWait * 1000L);
		return new JedisPool(poolConfig, host, port, timeout * 1000, password, database);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.jedisPool = getJedisPool();
	}

	/**
	 * 设置失效时间
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Long setnx(String key, String value) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.setnx(key, value);
		} catch (Exception e) {
			log.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return result;
	}

	/**
	 * 设置失效时间
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public String set(String key, String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.set(key, value);
		} catch (Exception e) {
			log.error("key失效：" + key);
			returnToPool(jedis);
			e.printStackTrace();
			return result;
		}
		jedis.close();
		return result;
	}

	/**
	 * 设置key的有效期，单位是秒
	 *
	 * @param key
	 * @param expireTime
	 * @return
	 */
	public Long expire(String key, int expireTime) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.expire(key, expireTime);
		} catch (Exception e) {
			log.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return result;
	}

	/**
	 * 获取key的value对象
	 *
	 * @param key
	 * @return String
	 */
	public String get(String key) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.get(key);
		} catch (Exception e) {
			log.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return result;
	}


	public String getset(String key, String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.getSet(key, value);
		} catch (Exception e) {
			log.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return result;
	}

	public Long del(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnToPool(jedis);
		}
		return Long.valueOf(0);
	}

	public List<String> scanKeys(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			List<String> keys = new ArrayList<String>();
			String cursor = "0";
			ScanParams sp = new ScanParams();
			sp.match("*" + key + "*");
			sp.count(100);
			do {
				ScanResult<String> ret = jedis.scan(cursor, sp);
				List<String> result = ret.getResult();
				if (result != null && result.size() > 0) {
					keys.addAll(result);
				}
				//再处理cursor
				cursor = ret.getCursor();
			} while (!cursor.equals("0"));
			return keys;
		} finally {
			returnToPool(jedis);
		}
	}

	private void returnToPool(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
}
