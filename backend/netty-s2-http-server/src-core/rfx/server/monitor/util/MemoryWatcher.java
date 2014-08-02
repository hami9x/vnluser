package rfx.server.monitor.util;

import java.util.ArrayList;
import java.util.List;

public class MemoryWatcher {
	private static final long MEGABYTE = 1024L * 1024L;

	public static double bytesToMegabytes(long bytes) {
		return roundOff2( (bytes * 1.0f / MEGABYTE) );
	}
	
	static double roundOff2(double a){
		return Math.round(a * 100.0) / 100.0;
	}
	
	static double roundOff6(double a){
		return Math.round(a * 1000000.0) / 1000000.0;
	}

	public static String collectMemoryStats() {
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		long memory = runtime.totalMemory() - runtime.freeMemory();
		StringBuilder s = new StringBuilder();
		
		s.append(" Used memory is bytes: " + memory);
		s.append(" Used memory is megabytes: " + bytesToMegabytes(memory));
		
		//double usedPercent=(double)(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/Runtime.getRuntime().maxMemory();
		//System.out.println(roundOff6(usedPercent));
		return s.toString();	    
	}

	public static void main(String[] args) throws InterruptedException {
		// I assume you will know how to create a object Person yourself...
		System.out.println(collectMemoryStats()); 
		List<String> list = new ArrayList<String>();
		for (int i = 0; i <= 4000000; i++) {
			list.add("this is a text");
		}
		//Thread.sleep(2000);
		System.out.println(collectMemoryStats());
		
		Thread.sleep(2000);
		list.clear();
		list = null;
		
		// Run the garbage collector		
		System.gc();
		
		Thread.sleep(2000);
		System.out.println(collectMemoryStats());
	}
}
