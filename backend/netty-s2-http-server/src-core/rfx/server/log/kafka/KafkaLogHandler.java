package rfx.server.log.kafka;

import io.netty.handler.codec.http.HttpRequest;

public interface KafkaLogHandler {
	public static final String logSocialActivityKafka = "kafka-social-activity-Producer";
	//public static final String logItemTrackingKafka = "kafka-item-tracking-Producer";
	
	//FIXME
	public static final String userid_bkKW   ="userid_bk%22%2C%22";
	public static final String		eatvKW   ="eatv%22%2C%22";   
	public static final String usersessionKW ="usersession%22%2C%22";

	/**
	 * Asynchronous push log data queue, the timer will schedule a job for sending to Kafka to avoid locking response
	 * 
	 * @param ip
	 * @param request
	 */
	public abstract void writeLogToKafka(String ip, String userAgent, String logDetails, String cookieString);

	public abstract void writeLogToKafka(String ip, HttpRequest request);
	
	public abstract void flushAllLogsToKafka(); 

	/**
	 * Use this method for sending mobile application log to Kafka.
	 * Asynchronous push log data queue, the timer will schedule a job for sending to Kafka to avoid locking response 
	 * 
	 * @param ip
	 * @param logDetails
	 */
	public abstract void writeMobileLogToKafka(String ip, HttpRequest request);
	
}