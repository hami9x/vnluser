package rfx.server.test.kafka;

import java.util.Random;

import org.junit.Test;

import rfx.server.log.kafka.HttpLogKafkaHandler;

public class AnalyticsLogProducer {

	@Test
	public void produceSampleKafkaXLogs() {
		try {
			long events = 5;
			Random rnd = new Random();		

			String cookieString = "visitor=6b45199c0f48bae4; ";
			String userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.71 Safari/537.36";
			
			// init
			HttpLogKafkaHandler.initKafkaSession();

			// get handler from factory
			HttpLogKafkaHandler kafkaHandler = HttpLogKafkaHandler.getKafkaHandler(HttpLogKafkaHandler.logSocialActivityKafka);
			for (long nEvents = 0; nEvents < events; nEvents++) {
				String ip = "192.168.2." + rnd.nextInt(255);
				String logDetails = " This is message for Kafka-X, blah..blah..";

				System.out.println(logDetails);

				kafkaHandler.writeLogToKafka(ip, userAgent, logDetails, cookieString);
			}
			// kafkaHandler.flushLogsToKafkaBroker();
			Thread.sleep(6000);
			// close to release resources
			kafkaHandler.shutdown();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}