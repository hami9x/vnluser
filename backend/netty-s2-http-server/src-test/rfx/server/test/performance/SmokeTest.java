package rfx.server.test.performance;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import rfx.server.util.http.HttpClientUtil;


public class SmokeTest {
	
	@Rule
	public ContiPerfRule i = new ContiPerfRule();
		
	static final String BASE = "http://localhost:8080/log/track?url=http://vnexpress.net/gl/khoa-hoc/2013/01/su-khac-biet-trong-nghien-cuu-giua-ta-va-tay/&title=aaa&t=";
	
	@Test
	@PerfTest(invocations = 30000, threads = 5060)
	@Required(max = 60000, average = 10000)
	public void smokeTestLocalLogServerUseNetty6k(){
		long t = System.currentTimeMillis();
		String url = BASE + t;
		String html = HttpClientUtil.executeGet(url);
		System.out.println(url + " => " + html);
		Assert.assertTrue(html.equals(""+t));
	}
	
	@Test
	@PerfTest(invocations = 50000, threads = 5000)
	@Required(max = 40000, average = 5000)
	public void smokeTestLocalLogServerUseNetty5k(){
		long t = System.currentTimeMillis();
		String url = BASE + t;
		String html = HttpClientUtil.executeGet(url);
		System.out.println(url + " => " + html);
		Assert.assertTrue(html.equals(""+t));
	}
	
	@Test
	@PerfTest(invocations = 10000, threads = 1000)
	@Required(max = 20000, average = 2000)
	public void smokeTestLocalLogServerUseNetty1k(){
		long t = System.currentTimeMillis();
		String url = BASE + t;
		String html = HttpClientUtil.executeGet(url);
		System.out.println(url + " => " + html);
		Assert.assertTrue(html.equals(""+t));
	}
	
	@Test
	@PerfTest(invocations = 20000, threads = 2000)
	@Required(max = 35000, average = 4000)
	public void smokeTestLocalLogServerUseNetty2k(){
		long t = System.currentTimeMillis();
		String url = BASE + t;
		String html = HttpClientUtil.executeGet(url);
		System.out.println(url + " => " + html);
		Assert.assertTrue(html.equals(""+t));
	}
	
	@Test
	@PerfTest(invocations = 1000, threads = 200)
	@Required(max = 30000, average = 4000)
	public void smokeTestLocalLogServerUseNetty200(){
		long t = System.currentTimeMillis();
		String url = BASE + t;
		String html = HttpClientUtil.executeGet(url);
		System.out.println(url + " => " + html);
		Assert.assertTrue(html.equals(""+t));
	}
	
	

}
