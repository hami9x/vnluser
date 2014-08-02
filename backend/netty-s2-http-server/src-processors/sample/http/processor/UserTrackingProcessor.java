package sample.http.processor;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.log.kafka.HttpLogKafkaHandler;
import rfx.server.log.kafka.KafkaLogHandler;

@HttpProcessorConfig(uriPath= "/tracking", contentType = ContentTypePool.TRACKING_GIF)
public class UserTrackingProcessor extends HttpProcessor {
	
	@Override
	protected DataService process(HttpRequestEvent e) {
		System.out.println("UserTrackingProcessor "+e.getRequest().getUri());
		String kafkaType =  HttpLogKafkaHandler.logSocialActivityKafka;
		KafkaLogHandler kafkaHandler = HttpLogKafkaHandler.getKafkaHandler(kafkaType );
		if(kafkaHandler != null){
			kafkaHandler.writeLogToKafka(e.getRemoteIp(), e.getRequest());
		}
		return EMPTY;
	}

}
