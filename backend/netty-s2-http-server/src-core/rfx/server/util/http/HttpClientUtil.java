package rfx.server.util.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import rfx.server.util.LogUtil;
import rfx.server.util.StringPool;





public class HttpClientUtil {	
		
	public static final Charset CHARSET_UTF8 = Charset.forName(StringPool.UTF_8);
	static int DEFAULT_TIMEOUT = 10000;//10 seconds
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:9.0) Gecko/20100101 Firefox/9.0";
	public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; DROID2 GLOBAL Build/S273) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	
	final static int MAX_SIZE = 20;
	static ConcurrentMap<Integer, HttpClient> httpClientPool = new ConcurrentHashMap<>(MAX_SIZE);
	
	public static final HttpClient getThreadSafeClient() throws Exception {
		int slot = (int)(Math.random() * (MAX_SIZE + 1));		
	    return getThreadSafeClient(slot);
	}
	
	public static final HttpClient getThreadSafeClient(int slot) throws Exception {		
		HttpClient httpClient = httpClientPool.get(slot);
		if(httpClient == null){
//			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//	        cm.setMaxTotal(40);	        
//	        httpClient = HttpClients.createMinimal().setConnectionManager(cm).build();
			httpClient = HttpClients.createMinimal();
		    httpClientPool.put(slot, httpClient);
		}
	    return httpClient;
	}
	
	public static boolean isValidHtml(String html){
		if(html == null){
			return false;
		}
		if (html.equals("404") || html.isEmpty() || html.equals("500")){
			return false;
		}
		return true;
	}

	
	public static String executePost(String url,Map<String, String> params, String accessTokens) {
		try {
			HttpClient httpClient = getThreadSafeClient();
			HttpPost postRequest = new HttpPost(url);			
			postRequest.addHeader("Accept-Charset", StringPool.UTF_8);
			postRequest.addHeader("User-Agent", USER_AGENT);
			postRequest.setHeader("Authorization", "OAuth oauth_token="+accessTokens);

			Set<String> names = params.keySet();
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(names.size());					
			for (String name : names) {
				System.out.println( name + "=" + params.get(name));
				postParameters.add(new BasicNameValuePair(name, params.get(name)));
			}			
			postRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			
			HttpResponse response = httpClient.execute(postRequest);
			HttpEntity entity = response.getEntity();
			if (entity != null) {			
				return EntityUtils.toString(entity, CHARSET_UTF8);
			}
		} catch (HttpResponseException e) {
			System.err.println(e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executeMultipartPost(String postUrl,Map<String, Object> params, String accessTokens)
			throws Exception {
		try {
			HttpClient httpClient = getThreadSafeClient();
			HttpPost postRequest = new HttpPost(postUrl);			
			postRequest.addHeader("Accept-Charset", StringPool.UTF_8);
			postRequest.addHeader("User-Agent", USER_AGENT);			
			postRequest.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			postRequest.setHeader("Authorization", "OAuth oauth_token="+accessTokens+", oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");

			MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
			Set<String> names = params.keySet();
			for (String name : names) {
				Object value = params.get(name);
				if (value instanceof StringBody) {
					reqEntity.addPart(name, (StringBody) value);
				} else if (value instanceof File) {
					reqEntity.addPart("media", new FileBody((File) value));
				}
			}
			postRequest.setEntity(reqEntity.build());

			HttpResponse response = httpClient.execute(postRequest);
			return EntityUtils.toString(response.getEntity(), CHARSET_UTF8);
		} catch (Exception e) {
			// handle exception here
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executePost(String url){
		try {	
			HttpPost httppost = new HttpPost(url);
			
			httppost.setHeader("User-Agent", USER_AGENT);
			httppost.setHeader("Accept-Charset", "utf-8");			
			httppost.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			httppost.setHeader("Authorization", "OAuth oauth_token=2d62f7b3de642cdd402f62e42fba0b25, oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");

			HttpResponse response = getThreadSafeClient().execute(httppost);
			HttpEntity entity = response.getEntity();				
			if (entity != null) {
				return EntityUtils.toString(entity, CHARSET_UTF8);
			}
		
		}  catch (HttpResponseException e) {
		    System.err.println(e.getMessage());		  
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return "";
	}
	
	public static String executeGet(final URL url){
		HttpResponse response = null;
		HttpClient httpClient = null;
		//System.out.println("executeGet:" + url);
		try {
			HttpGet httpget = new HttpGet(url.toURI());			
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");
			httpget.setHeader("Accept", "text/html,application/xhtml+xml");
			httpget.setHeader("Cache-Control", "max-age=3, must-revalidate, private");	
			
			//httpget.addHeader(BasicScheme.authenticate(	 new UsernamePasswordCredentials("ejgsadmin", "6uCdS7cA3"),"UTF-8", false));
			//httpget.setHeader("Authorization", "OAuth oauth_token=223a363ea1fd0a13b44e52663b97a255, oauth_consumer_key=a324957217164fd1d76b4b60d037abec, oauth_version=1.0, oauth_signature_method=HMAC-SHA1, oauth_timestamp=1322049404, oauth_nonce=-5195915877644743836, oauth_signature=wggOr1ia7juVbG%2FZ2ydImmiC%2Ft4%3D");
			
			httpClient = getThreadSafeClient();
			response = httpClient.execute(httpget);	
			
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {				
				HttpEntity entity = response.getEntity();
				if (entity != null) {										
					String html = EntityUtils.toString(entity, CHARSET_UTF8);
					return html;
				}
			} else if(code == 404) {
				return "404";
			} else {
				return "500";
			}
		}  catch (Throwable e) {						
			//e.printStackTrace();
			return "444";
		} finally {
			response = null;
		}
		return "";
	}
	
	public static String executeHttpGet(String urlString){
		HttpResponse response = null;
		HttpClient httpClient = null;		
		String html = StringPool.BLANK;
		int slot = (int)(Math.random() * (MAX_SIZE + 1));
		HttpGet httpget = null;
		try {
			URL url = new URL(urlString);
			httpget = new HttpGet(url.toURI());
			httpget.setHeader("User-Agent", USER_AGENT);
			httpget.setHeader("Accept-Charset", "utf-8");
			httpget.setHeader("Accept", "text/html,application/xhtml+xml");
			httpget.setHeader("Cache-Control", "max-age=3, must-revalidate, private");
			
			httpClient = getThreadSafeClient(slot);
			response = httpClient.execute(httpget);
			
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {				
				HttpEntity entity = response.getEntity();
				if (entity != null) {										
					html = EntityUtils.toString(entity, CHARSET_UTF8);					
				}
			} 
		}  catch (Exception e) {
			LogUtil.error("HttpClientUtil", e.toString() + ", fail http get to "+urlString);
			httpClientPool.remove(slot);			
		} finally {			
			response = null;			
		}
		return html;
	}
	
	public static String executeGet(final String url){
		try {
			return executeGet(new URL(url));
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		}
		return "";
	}
	
	public static String executeGet(final String url, boolean safeThread, boolean redownload500, int numRetry){		
		try {
			if(redownload500){
				String html = executeGet(new URL(url));
				while( html.equals("500") ){
					Thread.sleep(400);
					html = executeGet(new URL(url));					
					numRetry --;
					if(numRetry <= 0){
						break;
					}
				}				
				return html;
			} else {
				return executeGet(new URL(url));
			}
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return "";
	}	
	
	
	public static String executeGet(String baseUrl, Map<String, Object> params) {
		if( ! baseUrl.contains("?")){
			baseUrl += "?";
		}		
		StringBuilder url = new StringBuilder(baseUrl);
		Set<String> ps = params.keySet();
		int c=0, s=params.size()-1;
		try {
			for (String p : ps) {
				String v = URLEncoder.encode(params.get(p).toString().trim(), StringPool.UTF_8);
				if(!v.equals(StringPool.BLANK)){
					p = URLEncoder.encode(p, StringPool.UTF_8);					
					url.append(p).append("=").append(v);
					if(c<s){
						url.append("&");
					}
					c++;
				}
			}
		} catch (UnsupportedEncodingException e) {}
		System.out.println(url.toString());
		return executeGet(url.toString());
	}
	
	


	
	public static void main(String[] args) {
		String rs = HttpClientUtil.executeGet("http://vnexpress.net/");
		System.out.println(rs);
	}
	
	
}
