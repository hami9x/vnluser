package rfx.server.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MemoryManagementUtil {
	// http://www.concretepage.com/java/

	static boolean isStarted = false;

	public static class MemoryUsageTask extends TimerTask {
		public void run() {
			// using getMemoryPoolMXBeans
			System.out.println("result of getMemoryPoolMXBeans ");
			List<MemoryPoolMXBean> mpmxList = ManagementFactory
					.getMemoryPoolMXBeans();

			for (MemoryPoolMXBean pl : mpmxList) {

				String name = pl.getName();
				System.out.println(name);

				MemoryUsage mu = pl.getPeakUsage();

				System.out.println("---using MemoryUsage---");

				// memory that can be used by JVM
				System.out.println(mu.getCommitted());

				// memory that is being used by JVM
				System.out.println(mu.getUsed());

				// memory which has been request initially.
				System.out.println(mu.getInit());

				// max memory that can be requested.
				System.out.println(mu.getMax());

			}
		}
	}

	public static void startMemoryUsageTask() {
		if (!isStarted) {
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new MemoryUsageTask(), 1000, 10000);
			System.out.println("enableBenchmark = true, ShowCounterMap Thread started ...");
			isStarted = true;
		}
	}

}
