package top.naccl.gobang.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
	
	@Autowired
	private JedisPool jedisPool;

	/**
	 * 设置失效时间
	 * @param key
	 * @param value
	 * @return
	 */
	public Long setnx(String key ,String value) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.setnx(key, value);
		} catch (Exception e) {
			logger.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return  result;
	}

	/**
	 * 设置失效时间
	 * @param key
	 * @param value
	 * @return
	 */
	public String set(String key ,String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.set(key, value);
		} catch (Exception e) {
			logger.error("key失效：" + key);
			returnToPool(jedis);
			e.printStackTrace();
			return result;
		}
		jedis.close();
		return  result;
	}

	/**
	 * 设置key的有效期，单位是秒
	 * @param key
	 * @param expireTime
	 * @return
	 */
	public Long expire(String key, int expireTime){
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.expire(key, expireTime);
		} catch (Exception e) {
			logger.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
		return result;
	}

	/**
	 * 获取key的value对象
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
			logger.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
        return result;
    }


    public String getset(String key, String value){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.getSet(key, value);
        } catch (Exception e) {
			logger.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
        return result;
    }

    public Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
			logger.error("key失效：" + key);
			returnToPool(jedis);
			return result;
		}
		jedis.close();
        return result;
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
				if(result!=null && result.size() > 0) {
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
		 if(jedis != null) {
			 jedis.close();
		 }
	}
}
