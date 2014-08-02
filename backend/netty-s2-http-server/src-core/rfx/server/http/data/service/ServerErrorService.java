package rfx.server.http.data.service;

import rfx.server.http.data.DataServiceConfig;

@DataServiceConfig(template = "system/server-info")
public class ServerErrorService extends WebDataService {
	static final String classpath = ServerErrorService.class.getName();


	@Override
	public WebDataService build() {
		//TODO
		return this;
	}

	@Override
	public String getClasspath() {
		return classpath;
	}	
	
}
