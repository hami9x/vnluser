package rfx.server.http.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import rfx.server.configs.RedisPoolConfigs;
import rfx.server.util.StringUtil;


/**
 * 
 * the data manager from Subscribed Redis Channel
 * 
 * @author trieu
 *
 */
public class SubscribedChannelManager extends JedisPubSub {
	
	public static final String CHANNEL_FULL_STATS = "fullStats";
	public static final String CHANNEL_COMPACT_STATS = "compactStats";
    	
	private static Logger logger = LoggerFactory.getLogger(SubscribedChannelManager.class);
	WebSocketChannelManager wsChannelManager; 
	
	String channelName;		
	String message = "";

	public synchronized String getMessage() {
		return message;
	}

	public synchronized void setMessage(String fullStatsMessage) {
		this.message = fullStatsMessage;
	}
	

	public SubscribedChannelManager(String channelName) {
		super();	
		this.channelName = channelName;
		init();
	}

	void init() {
		wsChannelManager = WebSocketChannelManager.get(this.channelName); 
		
		RedisPoolConfigs redisPoolConfigs = RedisPoolConfigs.load(); 
        String host = redisPoolConfigs.getRealtimePubSub().get("host");
		int port = StringUtil.safeParseInt(redisPoolConfigs.getRealtimePubSub().get("port"));				
        JedisPool jedisPool = new JedisPool(redisPoolConfigs.getJedisPoolConfig(), host, port, 0);
 
        final Jedis subscriberJedis = jedisPool.getResource();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("--- Subscribing to channelName: \""+channelName+"\"");
					subscriberJedis.subscribe(SubscribedChannelManager.this, channelName);
					logger.info("Subscription ended.");
				} catch (Exception e) {
					logger.error("Subscribing failed.", e);
				}
			}
		}).start();
	}

	@Override
	public void onMessage(String channel, String message) {
		logger.info("Message received. Channel: {}, Msg: {}", channel, message);
		//wsChannelManager.broadcastMessage(message);
		setMessage(message);
		wsChannelManager.broadcastMessageToAllChannels(this.channelName,message);
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {

	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {

	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {

	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {

	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {

	}
}