package rfx.server.log.kafka;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rfx.server.configs.HttpServerConfigs;
import rfx.server.util.LogUtil;
import rfx.server.util.Utils;
import rfx.server.util.kafka.KafkaProducerUtil;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class LogBuffer extends TimerTask {

	private ProducerConfig producerConfig;
	private String topic = "";
	private String actorId;
	private Queue<HttpDataLog> queueLogs;
	private ExecutorService executor;
	int numberFlushOfJob = 3;
	
	public String getId() {
		return actorId;
	}

	public void setId(String id) {
		this.actorId = id;
	}
	
	public void push(HttpDataLog log){
		queueLogs.add(log);		
	}
	
	public LogBuffer(ProducerConfig producerConfig, String topic, int id) {
		super();
		HttpServerConfigs configs = HttpServerConfigs.load(); 
		this.producerConfig = producerConfig;
		this.topic = topic;		
		this.actorId = topic+"-"+id;
		this.queueLogs = new ConcurrentLinkedQueue<>();
		executor = Executors.newFixedThreadPool(configs.getSendKafkaThreadPerBatchJob());
	}
	
	public void flushLogsToKafkaBroker(boolean refreshProducer, int maxSize ){		
		int queueSize = this.queueLogs.size();
		if(maxSize <= 0){
			maxSize = queueSize;			
		} 
		
		if(queueSize > 0){
			try {
				//init the batch list with max capacity
				List<KeyedMessage<String, String>> batchLogs = new ArrayList<>(queueSize);
				int batchSize = 0;
				while( true ){									
					HttpDataLog log = this.queueLogs.poll();
					if(log == null){
						break;
					} else {					
						//build the payload, and add to the list
						System.out.println(log.toString());
						KeyedMessage<String, String> payload = new KeyedMessage<String, String>(this.topic, log.toString());	
						batchLogs.add(payload);						
						batchSize = batchLogs.size();
						if(batchSize >= maxSize && maxSize > 0){
							break;
						}
					}
				}
				//send as list, then force close connection
				if(batchSize > 0 && maxSize > 0){				
					System.out.println( "producer.send , batchLogs size : " + batchLogs.size());
					Producer<String, String> producer = KafkaProducerUtil.getKafkaProducer(actorId, producerConfig, refreshProducer);
					executor.execute(new FlushHttpDataLogsTask(actorId, producer, batchLogs));
				}
			} catch (Exception e) {			
				e.printStackTrace();
				LogUtil.error(topic, "sendToKafka fail : "+e.getMessage());			
			} 
		}
		
	}
	
	public void flushLogsToKafkaBroker(boolean refreshProducer ){			
		flushLogsToKafkaBroker(refreshProducer, HttpLogKafkaHandler.MAX_KAFKA_TO_SEND);
	}
	

	@Override
	public void run() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		//int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		boolean refreshProducer = (sec % 10) == 0 || (sec % 30) == 0;
			
		for (int i = 0; i < numberFlushOfJob; i++) {
			flushLogsToKafkaBroker(refreshProducer);
			if(refreshProducer){
				refreshProducer = false;
			}
			Utils.sleep(10);
		}
	}
}
