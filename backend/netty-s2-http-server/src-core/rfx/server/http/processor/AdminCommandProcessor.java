package rfx.server.http.processor;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.HttpProcessorManager;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.util.template.HandlebarsTemplateUtil;

@HttpProcessorConfig(privateAccess = HttpProcessorConfig.PRIVATE_ACCESS, uriPath = "/admin-command", contentType = ContentTypePool.JSON)
public class AdminCommandProcessor extends HttpProcessor {

	@Override
	protected DataService process(HttpRequestEvent requestEvent) {
		// HttpHeaders headers = request.headers();
		// String referer = headers.get(REFERER);
		// String userAgent = headers.get(USER_AGENT);
		// System.out.println("referer: "+referer);
		// System.out.println("userAgent: "+userAgent);

		String cmd = requestEvent.param("cmd", "");
		System.out.println("cmd: " + cmd);
		Object result = "fail";

		if (cmd.equals("refresh-templates")) {
			HandlebarsTemplateUtil.refreshCache();//TODO refactoring code to interface
			result = cmd + " success";
		} else if(cmd.equals("list-public-all-processors")){
			result = HttpProcessorManager.getUriMappingText();
		}
		return new AdminCommandResult(result);
	}

	static class AdminCommandResult implements DataService {
		static final String classpath = AdminCommandResult.class.getName();
		Object result;

		public AdminCommandResult(Object result) {
			super();
			this.result = result;
		}

		public Object getStatus() {
			return result;
		}

		public void setStatus(String status) {
			this.result = status;
		}

		@Override
		public void freeResource() {
			result = null;
		}
		
		@Override
		public boolean isRenderedByTemplate() {	
			//this is JSON text, 
			return false;
		}

		@Override
		public String getClasspath() {			
			return classpath;
		}

		@Override
		public List<HttpHeaders> getHttpHeaders() {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
