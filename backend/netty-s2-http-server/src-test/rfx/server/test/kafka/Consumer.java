package rfx.server.test.kafka;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;

public class Consumer extends Thread {
	private final ConsumerConnector consumer;
	private final String topic;

	public Consumer(String topic) {
		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig());
		this.topic = topic;
	}

	private static ConsumerConfig createConsumerConfig() {
		Properties props = new Properties();
		props.put("groupid", KafkaProperties.groupId);
		props.put("zk.connect", KafkaProperties.zkConnect);
		props.put("zk.sessiontimeout.ms", "400");
		props.put("zk.synctime.ms", "200");
		props.put("broker.list", "0:localhost:9092");
		props.put("autocommit.interval.ms", "1000");
		return new ConsumerConfig(props);
	}

	public void run() {
//		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
//		topicCountMap.put(topic, new Integer(1));
//		Map<String, List<KafkaStream<Message>>> consumerMap = consumer
//				.createMessageStreams(topicCountMap);
//		KafkaStream<Message> stream = consumerMap.get(topic).get(0);
//		ConsumerIterator<Message> it = stream.iterator();
//		while (it.hasNext())
//			System.out.println(getMessage(it.next().message()));
	}

	public static String getMessage(Message message) {
		ByteBuffer buffer = message.payload();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return new String(bytes);
	}
}
