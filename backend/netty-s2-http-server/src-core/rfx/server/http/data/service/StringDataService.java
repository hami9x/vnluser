package rfx.server.http.data.service;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

import rfx.server.configs.ContentTypePool;

public class StringDataService extends WebDataService{
	StringBuilder output;
	String contentType;

	@Override
	public void freeResource() {
		output.setLength(0);
	}

	@Override
	public String getClasspath() {		
		return DataService.getClasspath(this);
	}

	@Override
	public boolean isRenderedByTemplate() {
		return false;
	}

	@Override
	public List<HttpHeaders> getHttpHeaders() {
		return null;
	}
	
	public StringDataService(String out, String contentType) {
		output = new StringBuilder(out);
		this.contentType = contentType;
	}
	
	public StringDataService(String out) {
		output = new StringBuilder(out);		
	}
	
	public StringBuilder getOutput() {
		return output;
	}
	
	@Override
	public String toString() {
		return String.valueOf(output);
	}
	
	public String getContentType() {
		if(contentType == null){
			contentType = ContentTypePool.TEXT_UTF8;
		}
		return contentType;
	}
	
	public String getContentType(String defaultContentType) {
		return contentType == null ? defaultContentType : contentType;
	}

	@Override
	public WebDataService build() {
		this.httpHeaders.add(defaultHttpHeaders);	
		return this;
	}

}
