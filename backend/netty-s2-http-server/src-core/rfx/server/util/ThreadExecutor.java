package rfx.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutor {

	int maxSizeToClean = 100;
	int maxConcurrentThread = 1;
	int counter = 0;
	
	ExecutorService executor ;

	public ThreadExecutor(int maxSizeToClean, int maxConcurrentThread) {
		super();
		this.maxSizeToClean = maxSizeToClean;
		this.maxConcurrentThread = maxConcurrentThread;
		this.executor = Executors.newFixedThreadPool(maxConcurrentThread);
	}
	
	public ThreadExecutor(int maxSizeToClean) {
		super();
		this.maxSizeToClean = maxSizeToClean;		
		this.executor = Executors.newSingleThreadExecutor();
	}
	
	public void execute(Runnable command){
		executor.execute(command);
		counter++;
		if(counter>=maxSizeToClean){
			executor.shutdown();	
			executor = null;
			if(maxConcurrentThread > 1){
				this.executor = Executors.newFixedThreadPool(maxConcurrentThread);	
			} else {
				this.executor = Executors.newSingleThreadExecutor();
			}
			
		}
	}
	
	public void shutdown(){
		executor.shutdown();	
	}
}
