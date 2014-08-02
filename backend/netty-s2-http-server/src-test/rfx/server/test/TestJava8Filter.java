package rfx.server.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import rfx.server.util.StringUtil;
import rfx.server.util.Utils;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;

public class TestJava8Filter {
	static class Tuple {
		List<Integer> data;
		public Tuple(int size) {
			data = new ArrayList<Integer>(size);
		}
		
		public void addData(int i){
			data.add(i);
		}
		
		@Override
		public String toString() {
			return new Gson().toJson(this);
		}
	}
	
	static Function<String, Tuple> functor1 = new Function<String, Tuple>() {		
		@Override
		public Tuple apply(String t) {
			
			//System.out.println(t);
			String msg = indexedData.remove(t);
			
			//System.out.println("functor1 "+t + " => " + msg);
			if(msg != null){
				Tuple tuple = new Tuple(1);
				tuple.addData(StringUtil.safeParseInt(msg.replace("message-", "")));
				return tuple;
			}
			return null;
		}
	};
	
	static Function<Tuple, Void> functor2 = new Function<Tuple, Void>() {
		@Override
		public Void apply(Tuple t) {
			//System.out.println("functor2 "+t);			
			return null;
		}
	};
	
	static AtomicLong totalProcessedCount = new AtomicLong(0);
	static AtomicBoolean stopApp = new AtomicBoolean(false);
	static List<Integer> data = new ArrayList<Integer>();
	//static Queue<Integer> queue = new ConcurrentLinkedQueue<Integer>();
	static Map<String, String> indexedData = new ConcurrentHashMap<String, String>();

	public static void main(String[] args) throws Exception {
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		int MAX = 1000000;
		int MAX_STOP = 1000000;
		int STREAM_SIZE = 2000;
		int SEED_SIZE = 50;
//		for (int i = 0; i < MAX; i++) {
//			data.add(Utils.randInt(0, MAX));
//		}
//		System.out.println(data.size());
		
		new Thread( () -> {
			while(true){		
				if(TestJava8Filter.stopApp.get()){
					break;
				}
				
				String k = Utils.randomUniqueString();
				String v = "message-"+Utils.randInt(0,MAX);  
				indexedData.put(k,v);
							
				if(indexedData.size() % SEED_SIZE == 0) {
					Utils.sleep(1);	
				}
				
				long allTotal = totalProcessedCount.get();
				if(allTotal > MAX_STOP){
					TestJava8Filter.stopApp.set(true);
					System.out.println("****stopApp = true at totalProcessedCount "+allTotal);
				}
			}
		} ).start();
		
		
//		List<Tuple> rsStream = data.parallelStream().map( i -> {
//			System.out.println(i);
//			Tuple tuple = new Tuple(1);
//			tuple.addData(i);
//			return tuple;
//		}).collect(Collectors.toCollection(ArrayList::new));		
//		System.out.println(rsStream);
		
//		long count = data.parallelStream().limit(MAX).map(functor1).map(functor2).count();
//		System.out.println(count);
		int poolSize = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		for (int i = 1; i <= 20; i++) {
			final int id = i;
			executorService.execute( () -> {
				System.out.println("Worker "+id);
				while(true){	
					if(indexedData.size()==0){
						Utils.sleep(20);
						if(indexedData.size()==0){
							break;
						}
					}
					long count = indexedData.keySet().parallelStream().limit(STREAM_SIZE)
									.map(functor1).filter(t -> t != null).map(functor2).count();
					totalProcessedCount.addAndGet(count);
					System.out.println("processed count = "+count);
					System.out.println("remain indexedData.size = "+indexedData.size());
					System.out.println("Thread.activeCount = "+Thread.activeCount());
					Utils.sleep(1);
				}
			});
		}		
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			Utils.sleep(1);
		}
		
		
		System.out.println("Thread.activeCount = "+Thread.activeCount());		
		System.out.println("totalProcessedCount = "+totalProcessedCount.get());
		System.out.println("remain indexedData.size = "+indexedData.size());
		long donetime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
		System.out.println("donetime= "+donetime);
		double avg = totalProcessedCount.get() / donetime;
		System.out.printf("1 MILLISECONDS can process %f messages",avg);
//		
		
	}
}

//totalProcessedCount = 1000006
//donetime= 22188
//1 MILLISECONDS can process 45.000000 messages
