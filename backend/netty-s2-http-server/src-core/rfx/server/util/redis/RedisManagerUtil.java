package rfx.server.util.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import rfx.server.configs.RedisPoolConfigs;
import rfx.server.util.StringPool;

public class RedisManagerUtil {
	static Logger logger = Logger.getLogger(RedisManagerUtil.class);

	static ShardedJedisPool redisIPLocationPool;

	static ExecutorService redisExecutor = Executors.newSingleThreadExecutor();

	private static RedisPoolConfigs redisConfigs;
	static {
		redisConfigs = RedisPoolConfigs.load();

		//  IP => location
		String host = redisConfigs.getIPLocation().get("host");
		int port = Integer.parseInt(redisConfigs.getIPLocation().get("port"));
		List<JedisShardInfo> redisIPLocationShard = new ArrayList<JedisShardInfo>();
		redisIPLocationShard.add(new JedisShardInfo(host, port, 0));
		redisIPLocationPool = new ShardedJedisPool(redisConfigs.getJedisPoolConfig(), redisIPLocationShard);
	}

	public static ShardedJedisPool getIPLocationPool() {
		return redisIPLocationPool;
	}

	public static RedisPoolConfigs getRedisPoolConfigs() {
		return redisConfigs;
	}

	public static Jedis getInstance(String host) {
		return new Jedis(host);
	}

	/**
	 * the redis at localhost
	 * 
	 * @return
	 */
	public static Jedis getLocalHostRedis() {
		return new Jedis("localhost");
	}

	public static void dumpLogHashMap(final Jedis jedis, final String hashmapName) {
		System.out.println("---jedis.map: " + hashmapName);
		Set<String> set = jedis.hkeys(hashmapName);
		for (String s : set) {
			System.out.println(s + " - " + jedis.hget(hashmapName, s));
		}
	}

	public static boolean isRedisConnectionOk(final Jedis jedis) {
		if (jedis == null) {
			throw new IllegalArgumentException("Jedis param is null");
		}
		if (!jedis.isConnected()) {
			jedis.connect();
			return jedis.ping().equalsIgnoreCase("PONG");
		}
		return false;
	}

	public static void increaseIpAddressAndHttpPath(final String ipAdress, final String httpPath) {
		redisExecutor.execute(new Runnable() {
			@Override
			public void run() {
				ShardedJedisPool jedisPool = getIPLocationPool();
				ShardedJedis shardedJedis = null;
				boolean commited = false;
				try {
					shardedJedis = jedisPool.getResource();
					Jedis jedis = shardedJedis.getShard(StringPool.BLANK);

					Pipeline pipe = jedis.pipelined();
					//pipe.hincrBy("ip-monitor", ipAdress, 1L);
					pipe.hincrBy("httpPath-monitor", httpPath.replace("/", "-"), 1L);
					pipe.sync();

					commited = true;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (commited) {
						jedisPool.returnResource(shardedJedis);
					}
					else {
						jedisPool.returnBrokenResource(shardedJedis);
					}
				}				
			}	
		});
		
	}
	
	public static void logCounter(final String name) {
		redisExecutor.execute(new Runnable() {			
			@Override
			public void run() {
				ShardedJedisPool jedisPool = getIPLocationPool();
				ShardedJedis shardedJedis = null;
				boolean commited = false;
				try {
					shardedJedis = jedisPool.getResource();
					Jedis jedis = shardedJedis.getShard(StringPool.BLANK);

					Pipeline pipe = jedis.pipelined();			
					pipe.hincrBy("log-monitor", name, 1L);
					pipe.sync();

					commited = true;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (commited) {
						jedisPool.returnResource(shardedJedis);
					}
					else {
						jedisPool.returnBrokenResource(shardedJedis);
					}
				}
			}
		});		
	}
	
	
}
