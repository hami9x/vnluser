package rfx.server.http.data.service;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.ServerCookieEncoder;

import java.util.ArrayList;
import java.util.List;

import rfx.server.http.common.CookieUtil;
import rfx.server.util.StringUtil;



/**
 * 
 * the base model for HttpProcessor implementations, that use Template Engine for outputable text (the accessible Web for all browsers)
 *  
 * @author trieu <br>
 *
 */
public abstract class WebDataService implements DataService{	
		
	public abstract WebDataService build();
	protected List<HttpHeaders> httpHeaders = new ArrayList<>();	
	protected HttpHeaders defaultHttpHeaders = new DefaultHttpHeaders();
	
	@Override
	public boolean isRenderedByTemplate() {	
		return true;
	}
	
	@Override
	public String getClasspath() {
		return DataService.getClasspath(this);
	}
	
	@Override
	public void freeResource() {}
	
	@Override
	public List<HttpHeaders> getHttpHeaders() {
		return httpHeaders;
	}
	
	public void setHttpHeader(String name, String value) {
		if(StringUtil.isNotEmpty(name) && value != null){
			defaultHttpHeaders.add(name, value);	
		}
	}
	
	public void setCookie(String name, String value, String domain, String path, long maxAge) {
		if(StringUtil.isNotEmpty(name) && value != null){
			Cookie cookie = CookieUtil.createCookie(name, value, domain, path, maxAge);
			defaultHttpHeaders.add(Names.SET_COOKIE, ServerCookieEncoder.encode(cookie));
			defaultHttpHeaders.add(name, value);	
		}
	}
}

//http://www.javacreed.com/gson-annotations-example/