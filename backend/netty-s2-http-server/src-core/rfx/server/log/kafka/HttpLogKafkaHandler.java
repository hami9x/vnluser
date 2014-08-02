package rfx.server.log.kafka;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.REFERER;
import static io.netty.handler.codec.http.HttpHeaders.Names.USER_AGENT;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

import kafka.producer.ProducerConfig;
import rfx.server.configs.HttpServerConfigs;
import rfx.server.http.common.CookieUtil;
import rfx.server.util.LogUtil;
import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;
import rfx.server.util.kafka.KafkaProducerUtil;

public class HttpLogKafkaHandler implements KafkaLogHandler {
	
	static HttpServerConfigs httpServerConfigs = HttpServerConfigs.load();	
	
	//TODO optimize this config
	public final static int MAX_KAFKA_TO_SEND = 900;
	public final static long TIME_TO_SEND = 5000; //in milisecond
	public static int NUM_BATCH_JOB = httpServerConfigs.getNumberBatchJob();
	public static int SEND_KAFKA_THREAD_PER_BATCH_JOB = httpServerConfigs.getSendKafkaThreadPerBatchJob();
		
	private static Map<String, HttpLogKafkaHandler> _kafkaHandlerList = new HashMap<>();
	
	static boolean writeToKafka =  httpServerConfigs.getWriteKafkaLogEnable() == 1;
	AtomicLong counter = new AtomicLong();

	private List<LogBuffer> logBufferList = new ArrayList<>(NUM_BATCH_JOB);
	private Timer timer = new Timer();	
	private Random randomGenerator;
			
	private ProducerConfig producerConfig;

	private String topic = "";
	private int maxSizeBufferToSend = MAX_KAFKA_TO_SEND;
	
	public void countingToDebug() {
		long c = counter.addAndGet(1);
		System.out.println(this.topic + " logCounter: " + c);		
	}
	
	public static void initKafkaSession() {			
		try {
			Map<String,Map<String,String>> kafkaProducerList = httpServerConfigs.getKafkaProducerList();
			Set<String> keys = kafkaProducerList.keySet();
			System.out.println(keys);
			String defaultPartitioner = httpServerConfigs.getDefaultPartitioner();
			for (String key : keys) {
				Map<String,String> jsonProducerConfig = kafkaProducerList.get(key);
				String topic = jsonProducerConfig.get("kafkaTopic");
				String brokerList =  jsonProducerConfig.get("brokerList");			
				String partioner =  jsonProducerConfig.get("partioner");
				if (partioner == null ) {
					partioner = defaultPartitioner;
				}							
				Properties configs = KafkaProducerUtil.createProducerProperties(brokerList, partioner,MAX_KAFKA_TO_SEND);
				ProducerConfig producerConfig = new ProducerConfig(configs);
				HttpLogKafkaHandler kafkaInstance = new HttpLogKafkaHandler(producerConfig, topic);
				_kafkaHandlerList.put(key, kafkaInstance);
				LogUtil.info("KafkaHandler.init-loaded: "+ key + " => "+jsonProducerConfig);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	
	
	public void shutdown() {
		//executor.shutdown();
		//producer.close();		
		timer.cancel();
	}
	
	/*
	 * Get Singleton by specified config
	 */
	public static HttpLogKafkaHandler getKafkaHandler(String kafkaType){
		return _kafkaHandlerList.get(kafkaType);
	}
	
	protected HttpLogKafkaHandler(ProducerConfig producerConfig, String topic) {
		this.producerConfig 	= 	producerConfig;
		this.topic 				= 	topic;
		initTheTimer();
	}	
		
	public int getMaxSizeBufferToSend() {
		return maxSizeBufferToSend;
	}

	public void setMaxSizeBufferToSend(int maxSizeBufferToSend) {
		this.maxSizeBufferToSend = maxSizeBufferToSend;
	}

	void initTheTimer(){
		int delta = 400;
		for (int i = 0; i < NUM_BATCH_JOB; i++) {
			int id = i+1;
			LogBuffer buffer = new LogBuffer(producerConfig, topic,id);
			timer.schedule(buffer,delta , TIME_TO_SEND );
			delta += 100;
			logBufferList.add(buffer);
		}		
		randomGenerator = new Random();
	}	
	
	@Override
	public void writeLogToKafka(String ip, String userAgent, String logDetails, String cookieString){
		if( ! writeToKafka){
			//skip write logs to Kafka
			return;
		}		
		countingToDebug();			
		int index = randomGenerator.nextInt(logBufferList.size());		
		
		try {
			LogBuffer logBuffer = logBufferList.get(index);
			if(logBuffer != null){
				logBuffer.push(new HttpDataLog(ip, userAgent, logDetails, cookieString));
			} else {
				LogUtil.error(topic, "writeLogToKafka: FlushLogToKafkaTask IS NULL");
			}
		} catch (Exception e) {
			LogUtil.error(topic, "writeLogToKafka: "+e.getMessage());
		}
	}
	
	@Override
	public void flushAllLogsToKafka() {
		for (LogBuffer logBuffer : logBufferList) {
			//flush all
			logBuffer.flushLogsToKafkaBroker(false,-1);
		}		
	}
	

	@Override
	public void writeLogToKafka(String ip, HttpRequest request){	
		String uri = request.getUri();
		if(StringUtil.isEmpty(uri)){
			return;
		}
		int idx = uri.indexOf("?");
		if(idx < 0){
			return;
		}
		String logDetails = uri.substring(idx+1);
		if(StringUtil.isEmpty(logDetails)){
			return;
		}
		HttpHeaders headers = request.headers();
		String referer = headers.get(REFERER);
		String userAgent = headers.get(USER_AGENT);
		
		//cookie check & build
		String cookie = headers.get(COOKIE);		
		boolean shouldGenCookie = false;
		StringBuilder cookieStBuilder = new StringBuilder();
		if( StringUtil.isNotEmpty(cookie) ){
			try {
				cookie = URLDecoder.decode(cookie,StringPool.UTF_8);
			} catch (Exception e1) {}
			cookieStBuilder.append(cookie);
		} else {
			shouldGenCookie = true;
		}
		
		if(shouldGenCookie)
		{			
			String userid ="",eatv="",usersession="";			
			int sessionIx = logDetails.indexOf(usersessionKW);
			if( sessionIx > -1 ){
				sessionIx += usersessionKW.length();
				usersession = logDetails.substring( sessionIx , logDetails.indexOf("%", sessionIx+2)  ) ;
			}
			int useridIndex = logDetails.indexOf(userid_bkKW);
			if( useridIndex > -1 ){
				useridIndex += userid_bkKW.length();
				userid = logDetails.substring( useridIndex , logDetails.indexOf("%", useridIndex+2)  ) ;
			}
			
			// if userid empty ---> this browser not support cross-domain cookies and localStorage
			// create userid = hash(ip,browser,os)
			if( StringUtil.isEmpty(userid) ){
				String browser = userAgent;
				String os = "";
				userid = CookieUtil.generateUserIdByIp(ip, browser, os);
			}
			
			cookieStBuilder.append("userid=").append(userid);
			cookieStBuilder.append("; usersession=").append(usersession);
			cookieStBuilder.append("; eatv=").append(eatv);
		}
		
		if(StringUtil.isNotEmpty(referer)){
			cookieStBuilder.append("; referer=").append(referer);
		}
		
		writeLogToKafka(ip, userAgent, logDetails, cookieStBuilder.toString());		
	}
	
	@Override
	public void writeMobileLogToKafka(String ip, HttpRequest request) {
		if( ! writeToKafka){
			//skip write logs to Kafka
			return;
		}		
		countingToDebug();			
		int index = randomGenerator.nextInt(logBufferList.size());		
		
		try {
			LogBuffer logBuffer = logBufferList.get(index);
			if(logBuffer != null){
				String uri = request.getUri();
				if(StringUtil.isEmpty(uri)){
					return;
				}
				int idx = uri.indexOf("?");
				if(idx < 0){
					return;
				}
				String logDetails = uri.substring(idx+1);
				if(StringUtil.isEmpty(logDetails)){
					return;
				}
				logDetails = StringUtil.replace(logDetails, "%3A", ":");
				logBuffer.push(new HttpDataLog(ip, "-", logDetails, "-"));
			} else {
				LogUtil.error(topic, "writeLogToKafka: FlushLogToKafkaTask IS NULL");
			}
		} catch (Exception e) {
			LogUtil.error(topic, "writeLogToKafka: "+e.getMessage());
		}
	}


}
