package rfx.server.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rfx.server.configs.HttpServerConfigs;
import kafka.producer.KeyedMessage;



public class PersistentLogQueue {
	static final String TAG = PersistentLogQueue.class.getSimpleName();
	static final int POOL_MAX = HttpServerConfigs.load().getKafkaProducerList().size();
	static Map<String, PersistentQueue> persistentQueuePool = new HashMap<String, PersistentQueue>(POOL_MAX);
	
	final public static int MAX_QUEUE_SIZE = 500000; 
	final static String NAME = "analytics-log-queue";
	
	static {
		File d = new File(NAME);
		if( ! d.isDirectory() ){
			if( ! d.mkdir() ){
				System.err.println("Can not create dir:"+NAME);
				Utils.exit(200);				
			}
		}
	}
	
	
	
	static File createTempSubdir(String name,boolean cleanExistedFiles){		
		File d = new File(name);
		if( ! d.isDirectory() ){
			d.mkdir();
		} else {
			if(cleanExistedFiles){
				File[] files = d.listFiles();
				for (File file : files) {
					file.delete();
				}
			}
		}		
		return d;
	}
	
	public static String poll(String topic){
		try {
			PersistentQueue persistentQueue = persistentQueuePool.get(topic);
			if(persistentQueue != null){
				return persistentQueue.poll();
			}
		} catch (Exception e) {			
			e.printStackTrace();
			LogUtil.error(TAG, e.toString());
		}
		return null;
	}
	
	public static long size(String topic){
		PersistentQueue persistentQueue = persistentQueuePool.get(topic);
		if(persistentQueue != null){
			return persistentQueue.size();	
		}
		return 0;
	}
	
	public static String peek(String topic){
		try {
			PersistentQueue persistentQueue = persistentQueuePool.get(topic);
			if(persistentQueue != null){
				return persistentQueue.peek();
			}			
		} catch (Exception e) {			
			e.printStackTrace();
			LogUtil.error(TAG, e.toString());
		}
		return null;
	}
	
	public static boolean push(String topic, String log){
		try {
			PersistentQueue persistentQueue = persistentQueuePool.get(topic);
			if(persistentQueue != null){
				persistentQueue.push(log);
				LogUtil.info("logFileQueue.size: "+persistentQueue.size());
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.error(TAG, e.toString());
		}
		return false;
	}
	
	public static boolean push(List<KeyedMessage<String, String>> list){
		try {
			for (KeyedMessage<String, String> keyedMessage : list) {
				push(keyedMessage.topic(), keyedMessage.message());
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.error(TAG, e.toString());
		}
		return false;
	}
}
