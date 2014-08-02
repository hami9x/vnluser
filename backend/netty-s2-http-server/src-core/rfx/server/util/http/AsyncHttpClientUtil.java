package rfx.server.util.http;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class AsyncHttpClientUtil {
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";

	public static String asyncHttpGet(String url) {
		String body = "";
		try {
			AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder()
					.setFollowRedirects(true).setAllowPoolingConnection(true)
					.setUserAgent(userAgent).build();
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient(cf);
			Future<Response> f = asyncHttpClient.prepareGet(url).execute();
			Response r = f.get();
			body = (r.getResponseBody());
			asyncHttpClient.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return body;
	}
}
