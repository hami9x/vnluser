package rfx.server.log.kafka;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rfx.server.util.LogUtil;
import rfx.server.util.kafka.KafkaProducerUtil;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import com.google.common.base.Stopwatch;

public class FlushHttpDataLogsTask implements Runnable  {
	
	List<KeyedMessage<String, String>> batchLogs;
	private String actorId;
	Producer<String, String> producer;

	public FlushHttpDataLogsTask(String actorId, Producer<String, String> producer, List<KeyedMessage<String, String>> batchLogs) {		
		this.actorId = actorId;
		this.producer = producer;
		this.batchLogs = batchLogs;
		if(this.batchLogs == null){
			throw new IllegalArgumentException("batchLogs CAN NOT BE NULL");
		}
	}
	
	@Override
	public void run() {			
		if(producer != null && batchLogs.size()>0){
			try {		
				Stopwatch stopwatch = Stopwatch.createStarted();				
				producer.send(batchLogs);			
				stopwatch.stop();				
				long milis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
				System.out.println(this.actorId+" batchsize = "+batchLogs.size()+" to Kafka in milisecs = "+milis+"\n");	
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.error("FlushHttpDataLogsTask", "sendToKafka fail : "+e.getMessage());	
				//close & open the Kafka Connection manually				
				KafkaProducerUtil.closeAndRemoveKafkaProducer(actorId);
			} finally {
				batchLogs.clear();
				batchLogs = null;
			}	
		} else {
			LogUtil.error("FlushHttpDataLogsTask", "producer is NULL for actorId:" + actorId);
		}
		
	}
}