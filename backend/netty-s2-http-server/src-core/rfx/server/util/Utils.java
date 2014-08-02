package rfx.server.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Utils {

	public static void sleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {			
		}
	}
	
	public static void exit(){
		System.exit(1);
	}
	
	public static void exit(long delay){
		Timer timer = new Timer(); 
		timer.schedule(new TimerTask() {			
			@Override
			public void run() {
				Utils.exit();
			}
		}, delay);
	}
	
	public static String exec(String cmd) {
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println(pid);
				
		String rs = StringPool.BLANK;
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())); 
			StringBuffer sb = new StringBuffer(); 
			String line = reader.readLine();
			sb.append(line);
			while (line != null) {
				if(line != null){
					System.out.println(line);
					line = reader.readLine();
					sb.append(line);
				}				
			}
			rs = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return rs;
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

	    // Usually this should be a field rather than a method variable so
	    // that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static long randLong() {	    
	    Random rand = new Random(System.nanoTime());

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    long randomNum = Math.abs(rand.nextLong());

	    return randomNum;
	}
	
	public static String randomUniqueString(){
		return randomUniqueString(11);
	}
	
	public static String randomUniqueString(int maxLength){
		return StringUtil.safeSubString(Long.toString(Utils.randLong(), 36),maxLength);
	}
	
	public static void repeat(int n, Runnable r) {
		for (int i = 0; i < n; i++){
			r.run();
		}
	}
	
}
