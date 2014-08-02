package rfx.server.http.processor;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.http.data.service.ServerInfoService;

@HttpProcessorConfig(privateAccess = HttpProcessorConfig.PRIVATE_ACCESS, uriPath = "/server-info",contentType = ContentTypePool.HTML_UTF8)
public class ServerInfoProcessor extends HttpProcessor {

	@Override
	protected DataService process(HttpRequestEvent requestEvent) {
		// HttpHeaders headers = request.headers();
		// String referer = headers.get(REFERER);
		// String userAgent = headers.get(USER_AGENT);
		// System.out.println("referer: "+referer);
		// System.out.println("userAgent: "+userAgent);
		
		String filter = requestEvent.param("filter", "");
//		System.out.println("filter: " + filter);
		return new ServerInfoService(filter).build();
	}	

}
