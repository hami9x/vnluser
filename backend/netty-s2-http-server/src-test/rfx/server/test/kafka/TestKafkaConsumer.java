package rfx.server.test.kafka;

import java.util.ArrayList;
import java.util.List;

import rfx.server.log.kafka.SimplePartitioner;

public class TestKafkaConsumer {

	static double calculateAverage(List<Integer> list) {
		Integer sum = 0;
		for (Integer mark : list) {
			sum += mark;
		}
		return sum.doubleValue() / list.size();
	}

	public static void main(String[] args) {
		// Producer producerThread = new Producer(KafkaProperties.topic);
		// producerThread.start();

		// Consumer consumerThread = new Consumer(KafkaProperties.topic);
		// consumerThread.start();

		int MAX_SAMPLE = 100;
		List<Integer> list = new ArrayList<>(MAX_SAMPLE);
		int numPartition = 10;
		for (int i = 0; i < MAX_SAMPLE; i++) {
			SimplePartitioner partitioner = new SimplePartitioner();
			long t = System.currentTimeMillis() / 1000L;
			int par = partitioner.partition("" + t, numPartition);
			list.add(par);	
			System.out.println(par);
		}
		System.out.println("AVG: "+ calculateAverage(list));
		
	}
}
