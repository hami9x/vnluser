package rfx.server.test.performance;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import rfx.server.util.http.HttpClientUtil;

import com.google.common.base.Stopwatch;

public class StressTestHttpServer {
	static AtomicInteger validCount = new AtomicInteger(0);
	static AtomicInteger invalidCount = new AtomicInteger(0);
	static Stopwatch stopwatch = Stopwatch.createUnstarted();

	@Rule
	public ContiPerfRule i = new ContiPerfRule();
	//https://github.com/LMAX-Exchange/disruptor
	
	@Test
	@PerfTest(invocations = 50000, threads = 500)
	@Required(max = 10000, average = 1000)
	public void test2() throws Exception {
		//String url = "http://localhost:9090/hello?name=Albert%20Einstein";
		String url = "http://localhost:31000/server-info?filter=compact";
		String rs = HttpClientUtil.executeGet(url);

		if (rs.length()>3) {
			int c = validCount.incrementAndGet();
			System.out.println(c+" \n");
		} else {
			System.out.println(rs);
			invalidCount.incrementAndGet();
			throw new IllegalArgumentException("Bad response!");
		}
		
//		Throughput:	3,835 / s	
//		Min. latency:	0 ms	
//		Average latency:	128 ms	400 ms
//		Median:	80 ms	
//		90%:	286 ms	
//		Max latency:	2,516 ms	4,000 ms
	}
	
	@Before
	public void beginTest(){
		stopwatch.start();
		System.out.println("-------------------------------------");
		System.out.println("valid " + validCount.get());
		System.out.println("invalid " + invalidCount.get());
		System.out.println("-------------------------------------");
	}
	
	@After
	public void finishTest(){
		System.out.println("-------------------------------------");
		System.out.println("valid " + validCount.get());
		System.out.println("invalid " + invalidCount.get());
		System.out.println("-------------------------------------");
		stopwatch.stop();
		System.out.println("finished in milliseconds: "+stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}
	
	//Throughput: 2,351 messages / second ~ 203,126,400 messages / day

}
