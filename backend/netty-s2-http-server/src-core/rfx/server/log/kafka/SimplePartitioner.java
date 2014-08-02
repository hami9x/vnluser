package rfx.server.log.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class SimplePartitioner implements Partitioner {

	static final int MIN = 0;

	public SimplePartitioner() {

	}

	public SimplePartitioner(VerifiableProperties props) {
		// System.out.println(props);
	}

	/*
	 * Randomly write to kafka
	 */
	@Override
	public int partition(Object key, int a_numPartitions) {
		int partition = 0;
		// int offset = key.lastIndexOf('.');
		// if (offset > 0) {
		// partition = Integer.parseInt(key.substring(offset + 1))
		// % a_numPartitions;
		// }
		int MAX = a_numPartitions - 1;
		partition = MIN + (int) (Math.random() * ((MAX - MIN) + 1));
		System.out.println("a_numPartitions: " + a_numPartitions
				+ " partitionId: " + partition);
		return partition;
	}

}
