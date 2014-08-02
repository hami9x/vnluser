package rfx.server.http;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;


/**
 * the base class for HTTP processor, input: HttpRequest output: processed model object
 * 
 * @author Trieu.nguyen
 *
 */
public abstract class HttpProcessor {
	
	
	public static final DataService EMPTY = new DataService() {
		@Override
		public boolean isRenderedByTemplate() {			
			return false;
		}		
		@Override
		public void freeResource() {}
		@Override
		public String getClasspath() {			
			return DataService.class.getName();
		}
		@Override
		public List<HttpHeaders> getHttpHeaders() {
			return null;
		}
	};
	
	public static DataService redirect(String url){
		return new RedirectService(url);
	}
	
	public static class RedirectService implements DataService {
		String redirectedUrl;
		
		public RedirectService(String redirectedUrl) {
			super();
			this.redirectedUrl = redirectedUrl;
		}

		public String getRedirectedUrl() {
			return redirectedUrl;
		}
		
		@Override
		public boolean isRenderedByTemplate() {			
			return false;
		}		
		@Override
		public void freeResource() {}
		@Override
		public String getClasspath() {			
			return DataService.class.getName();
		}
		@Override
		public List<HttpHeaders> getHttpHeaders() {
			return null;
		}
	};
	
	/**
	 * always called by HttpProcessorManager
	 * 
	 * @return processed model object
	 */
	public DataService doProcessing(HttpRequestEvent event) {
		//TODO support hooking, filtering HttpRequestEvent
		
		//call the implemented process method
		return process(event);
	}

	
	///////////// for the implementation class /////////////
	protected abstract DataService process(HttpRequestEvent event);

}
