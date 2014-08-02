package rfx.server.util.template;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rfx.server.http.data.service.DataService;

public class MustacheServiceModel extends HashMap<String, Object> implements DataService{
	static final String classpath = MustacheServiceModel.class.getName();
	private static final long serialVersionUID = 5072021270346354454L;
	
	public MustacheServiceModel(){}

	public MustacheServiceModel(int numberField){
		super(numberField);
	}
	
	public MustacheServiceModel(Map<String, Object> obj){
		super(obj);
	}
	
	public void set(String name, Object val){
		this.put(name, val);
	}

	@Override
	public void freeResource() {
		clear();
	}

	@Override
	public boolean isRenderedByTemplate() {		
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
