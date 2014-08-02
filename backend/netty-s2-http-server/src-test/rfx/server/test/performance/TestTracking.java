package rfx.server.test.performance;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub;
import rfx.server.util.http.HttpClientUtil;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class TestTracking {

	public static void main(String[] args) throws Exception {
//		String url = "http://localhost:9090/item-tracking?url=http%3A%2F%2Fit-ebooks.info%2Fbook%2F3614%2F&referer=http%3A%2F%2Fit-ebooks.info%2F&title=Beginning+Programming+with+Java+For+Dummies%2C+4th+Edition+-+Free+Download+eBook+-+pdf&html=Beginning%2520Programming%2520with%2520Java%2520For%2520Dummies%2C%25204th%2520Edition%2520is%2520a%2520comprehensive%2520guide%2520to%2520learning%2520one%2520of%2520the%2520most%2520popular%2520%253Ca%2520href%3D%2522http%3A%2F%2Fit-ebooks.info%2Ftag%2Fprogramming%2F%2522%2520title%3D%2522Programming%2520eBooks%2522%253Eprogramming%253C%2Fa%253E%2520languages%2520worldwide.%2520This%2520book%2520covers%2520%253Ca%2520href%3D%2522http%3A%2F%2Fit-ebooks.info%2Ftag%2Fbasic%2F%2522%2520title%3D%2522Basic%2520eBooks%2522%253Ebasic%253C%2Fa%253E%2520development%2520concepts%2520and%2520techniques%2520through%2520a%2520%253Ca%2520href%3D%2522http%3A%2F%2Fit-ebooks.info%2Ftag%2Fjava%2F%2522%2520title%3D%2522Java%2520eBooks%2522%253EJava%253C%2Fa%253E%2520lens.%2520You%27ll%2520learn%2520what%2520goes%2520into%2520a%2520program%2C%2520how%2520to%2520put%2520the%2520pieces%2520together%2C%2520how%2520to%2520deal%2520with%2520challenges%2C%2520and%2520how%2520to%2520make%2520it%2520work.%2520The%2520new%2520Fourth%2520Edition%2520has%2520been%2520updated%2520to%2520align%2520with%2520Java%25208%2C%2520and%2520includes%2520new%2520options%2520for%2520the%2520latest%2520tools%2520and%2520techniques.%253Cbr%253E";
//		String rs = HttpClientUtil.executeGet(url);
//		System.out.println(rs);
		
		String title = "", content = "";
		String targetUrl = "http://techcrunch.com/2014/07/30/solving-optimal-health-for-googlex-in-north-carolina/";
		Document doc = Jsoup.connect(targetUrl).get();
		title = doc.title();
		content =  ArticleExtractor.INSTANCE.getText(doc.select("body").html());
		
		StringBuilder guessUrl = new StringBuilder("http://localhost:8888/keywords?");
		guessUrl.append("title=").append( URLEncoder.encode(title, "UTF-8"));
		guessUrl.append("&content=").append(URLEncoder.encode(content, "UTF-8"));
		
		String rs = HttpClientUtil.executeGet(guessUrl.toString());
		System.out.println(guessUrl.toString());
		System.out.println(rs);
		
//		Jedis jedis = new Jedis("localhost");
//		jedis.publish("test", "this is from java");
	}
	
	public static class Subscriber extends JedisPubSub {
		 
	    private static Logger logger = LoggerFactory.getLogger(Subscriber.class);
	 
	    @Override
	    public void onMessage(String channel, String message) {
	        logger.info("Message received. Channel: {}, Msg: {}", channel, message);
	    }
	 
	    @Override
	    public void onPMessage(String pattern, String channel, String message) {
	 
	    }
	 
	    @Override
	    public void onSubscribe(String channel, int subscribedChannels) {
	 
	    }
	 
	    @Override
	    public void onUnsubscribe(String channel, int subscribedChannels) {
	 
	    }
	 
	    @Override
	    public void onPUnsubscribe(String pattern, int subscribedChannels) {
	 
	    }
	 
	    @Override
	    public void onPSubscribe(String pattern, int subscribedChannels) {
	 
	    }
	}
}
