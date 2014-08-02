package sample.save2dropbox.processor;

import java.net.URLDecoder;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;

/**
 * @author trieu
 *
 */
@HttpProcessorConfig(uriPath= "/item-tracking", contentType = ContentTypePool.TRACKING_GIF)
public class ItemTrackingProcessor extends HttpProcessor {
	
	@Override
	protected DataService process(HttpRequestEvent e) {
		System.out.println("UserTrackingProcessor "+e);
		
		try {
			String url = e.param("url");
			String referer = e.param("referer");
			String title = e.param("title");
			String html = URLDecoder.decode(e.param("html"),"UTF-8");
			System.out.println(url);
			System.out.println(referer);
			System.out.println(title);
			System.out.println(html);
		} catch (Exception e1) {
		}
		return EMPTY;
	}

}
