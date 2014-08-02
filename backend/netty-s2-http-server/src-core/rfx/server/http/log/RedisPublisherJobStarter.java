package rfx.server.http.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import redis.clients.jedis.Jedis;
import rfx.server.configs.RedisInfo;
import rfx.server.configs.RedisPoolConfigs;
import rfx.server.util.DateTimeUtil;
import rfx.server.util.StringUtil;

import com.google.gson.Gson;

public class RedisPublisherJobStarter {

    public static final String CHANNEL_NAME = "commonChannel";
    volatile static Jedis publisherJedis, jedis6386, jedis6384, jedis6383, jedis6381;
    static Timer timer = new Timer(true);
    final static RedisInfo clusterredisInfo;

    static {
	RedisPoolConfigs redisPoolConfigs = RedisPoolConfigs.load();

	String host = redisPoolConfigs.getRealtimePubSub().get("host");
	int port = StringUtil.safeParseInt(redisPoolConfigs.getRealtimePubSub().get("port"));
	clusterredisInfo = new RedisInfo(host, port);
    }

    static void startRealtimeDataPublisher(final String[] args) {
	System.out.println(args.length);
	if (args.length < 6) {
	    System.out.println("Invalid Param, use: 10.254.53.17 45386 10.254.53.17 45381 10.254.53.17 45383");
	    return;
	}
	final String host1 = args[0];
	final int port1 = StringUtil.safeParseInt(args[1]);

	final String host2 = args[2];
	final int port2 = StringUtil.safeParseInt(args[3]);

	final String host3 = args[4];
	final int port3 = StringUtil.safeParseInt(args[5]);

	timer.schedule(new TimerTask() {

	    @Override
	    public void run() {
		try {
		    if (publisherJedis == null) {
			publisherJedis = new Jedis(clusterredisInfo.getHost(), clusterredisInfo.getPort(), 0);
			publisherJedis.connect();
		    }
		    if (jedis6386 == null) {
			jedis6386 = new Jedis(host1, port1, 0);
			jedis6386.auth("Homnaylathu6@yeah");
			jedis6386.connect();
		    }
		    if (jedis6381 == null) {
			jedis6381 = new Jedis(host2, port2, 0);
			jedis6381.connect();
		    }
		    if (jedis6383 == null) {
			jedis6383 = new Jedis(host3, port3, 0);
			jedis6383.connect();
		    }

		    Map<String, Object> map = new HashMap<String, Object>(12);
		    Date d = new Date();
		    String hour = DateTimeUtil.getDateHourString(d);
		    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
		    map.put("datehourval", dateFormat.format(d));

		    map.put("total-pageview", jedis6386.hget("summary-pageview", "monitor:pv"));
		    map.put("total-pageview-1hour", jedis6386.hget("t:" + hour, "monitor:pv"));

		    map.put("total-impression", jedis6386.hget("summary-impression", "monitor:i"));
		    map.put("total-impression-1hour", jedis6386.hget("t:" + hour, "monitor:i"));
		    map.put("total-impression-1minute", 0);
		    map.put("total-impression-1second", 0);

		    map.put("total-click", jedis6381.hget("cpc_compaigns", "monitor:c"));
		    map.put("total-click-1hour", jedis6381.hget("t:" + hour, "monitor:c"));
		    map.put("total-click-1minute", 0);
		    map.put("total-click-1second", 0);

		    map.put("total-trueimpression", jedis6383.hget("campaigns", "monitor:i"));
		    map.put("total-trueimpression-1hour", jedis6383.hget("t:" + hour, "monitor:i"));

		    String message = new Gson().toJson(map);
		    publisherJedis.publish(CHANNEL_NAME, message);

		    int sleepRandom = (int) (Math.random() * (4 + 1));
		    Thread.sleep(sleepRandom * 1000);
		} catch (Exception e) {
		    e.printStackTrace();
		    try {
			publisherJedis.disconnect();
			jedis6386.disconnect();
			jedis6381.disconnect();
			jedis6383.disconnect();

			publisherJedis = null;
			jedis6386 = null;
			jedis6381 = null;
			jedis6383 = null;
			Thread.sleep(500);
		    } catch (Exception e1) {
		    }
		}
	    }
	}, 2000, 1000);
    }

    static void testRealtimePublisher() {
	timer.schedule(new TimerTask() {
	    @Override
	    public void run() {
		try {
		    if (publisherJedis == null) {
			publisherJedis = new Jedis(clusterredisInfo.getHost(), clusterredisInfo.getPort(), 0);
			publisherJedis.connect();
		    }
		    Map<String, Object> map = new HashMap<String, Object>(12);
		    Date d = new Date();
		    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
		    map.put("datehourval", dateFormat.format(d));

		    map.put("total-impression-1hour", System.currentTimeMillis());

		    String message = new Gson().toJson(map);
		    publisherJedis.publish(CHANNEL_NAME, message);

		    int sleepRandom = (int) (Math.random() * (4 + 1));
		    Thread.sleep(sleepRandom * 1000);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}, 2000, 1000);
    }

    public static void main(String[] args) {

	testRealtimePublisher();
	while (true) {
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
	    }
	}
    }
}
