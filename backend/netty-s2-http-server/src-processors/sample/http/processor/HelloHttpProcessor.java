package sample.http.processor;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.http.data.service.StringDataService;

/**
 * @author trieu
 * 
 * simple sample processor 
 *
 */
@HttpProcessorConfig(uriPath= "/hello", contentType = ContentTypePool.JSON)
public class HelloHttpProcessor extends HttpProcessor {
	
	@Override
	protected DataService process(HttpRequestEvent requestEvent) {		
		String name = requestEvent.param("name", "guest");
		System.out.println("name: " + name);
		return new StringDataService(name);
	}

}
