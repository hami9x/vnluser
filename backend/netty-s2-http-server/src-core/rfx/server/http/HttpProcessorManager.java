package rfx.server.http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.Reflections;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor.RedirectService;
import rfx.server.http.common.NettyHttpUtil;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.http.data.service.StringDataService;
import rfx.server.log.handlers.StaticFileHandler;
import rfx.server.util.RoundRobin;
import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;
import rfx.server.util.template.DataServiceProcessingUtil;

/**
 * @author Trieu.nguyen
 * 
 * the manager, the factory for HTTP processor instances, input: HttpRequest output: FullHttpResponse
 *
 */
public class HttpProcessorManager {
	
	private String contentType;	
	Class<?> httpProcessorClass;
	RoundRobin<HttpProcessor> roundRobinRounter;
	static Map<String,String> uriMappingText = new HashMap<>();
	static boolean autoOptimizeOutput = false;
	
	public static void setUriMappingText(String uri, String description) {		
		 uriMappingText.put(uri, description);
	}
	
	public static Map<String, String> getUriMappingText() {		
		return uriMappingText;
	}
		
	public HttpProcessorManager(String contentType, Class<?> httpProcessorClass, int maxPoolSize) throws Exception {
		super();	
		initPool(contentType,httpProcessorClass, maxPoolSize);
	}
	
	void initPool(String contentType, Class<?> httpProcessorClass, int maxPoolSize) throws InstantiationException, IllegalAccessException {
		this.contentType = contentType;		
		this.httpProcessorClass = httpProcessorClass;
		
		if(maxPoolSize == 1){
			roundRobinRounter = new RoundRobin<>((HttpProcessor) httpProcessorClass.newInstance());
		} else {
			List<HttpProcessor> pool = new ArrayList<>(maxPoolSize);
			for (int i = 0; i < maxPoolSize; i++) {
				HttpProcessor httpProcessor = (HttpProcessor) httpProcessorClass.newInstance();
				pool.add(httpProcessor);
			}
			roundRobinRounter = new RoundRobin<>(pool);	
		}
	}
	

	/**
	 * the main router for all processors
	 * 
	 * @return FullHttpResponse
	 */
	public FullHttpResponse doProcessing(HttpRequestEvent event) {		
		DataService dataServe = null;
		FullHttpResponse response = null;
		HttpProcessor processor;
		try {
			processor = roundRobinRounter.next();
			dataServe = processor.doProcessing(event);
			if(dataServe instanceof RedirectService){
				String url = ((RedirectService)dataServe).getRedirectedUrl();
				response = NettyHttpUtil.redirect(url);
			} 
			else if(dataServe instanceof StringDataService){
				StringDataService stringDataService = (StringDataService) dataServe;
				response = NettyHttpUtil.theHttpContent(stringDataService.toString(), stringDataService.getContentType(contentType));
			}
			else {
				switch (contentType) {
					case ContentTypePool.TRACKING_GIF:
						response = StaticFileHandler.theBase64Image1pxGif();
						break;
					case ContentTypePool.JSON:
						String json = StringUtil.convertObjectToSafeJson(dataServe);
						response = NettyHttpUtil.theHttpContent(json, contentType);
						break;
					default:
						response = DataServiceProcessingUtil.processOutput(event, dataServe, contentType);
						break;
				}
			}
		} 
		catch (Throwable e) {						
			e.printStackTrace();
			StringBuilder s = new StringBuilder("Error###");
			s.append(e.getMessage());
			s.append(" ### <br>\n StackTrace: ").append(ExceptionUtils.getStackTrace(e));							
			response = NettyHttpUtil.theHttpContent(s.toString());			
		} 
		finally {
			if(dataServe != null){
				dataServe.freeResource();
			}
		}
		if(response != null){
			return response;
		}
		return NettyHttpUtil.theHttpContent(StringPool.BLANK);
	}
	
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}


	/**
	 * run at server bootstrap to init the pool of Http Event Processor
	 * @throws Exception 
	 */
	public static Map<String, HttpProcessorManager> initProcessorPool(String classpath, int filteredAccessMode, int processorPoolSize) throws Exception {	
		Reflections reflections = new Reflections(classpath);
		Set<Class<?>> clazzes =  reflections.getTypesAnnotatedWith(HttpProcessorConfig.class);
		Map<String, HttpProcessorManager> tempMap = new HashMap<>();
		
		System.out.println("----------------------------initProcessorPool-----------------------------------");
		String accessMode = (filteredAccessMode == HttpProcessorConfig.PUBLIC_ACCESS ? "PUBLIC" : "PRIVATE");
		System.out.println(" Access Mode "+ accessMode);
		System.out.println("...classpath \""+ classpath + "\" processorPoolSize = "+ processorPoolSize);
		System.out.println("--------------------------------------------------------------------------------------");
		
	    for (Class<?> clazz : clazzes) {
			if (clazz.isAnnotationPresent(HttpProcessorConfig.class)) {        		     
				Annotation annotation = clazz.getAnnotation(HttpProcessorConfig.class);
				HttpProcessorConfig config = (HttpProcessorConfig) annotation;
				
				if( config.privateAccess() == filteredAccessMode){
					HttpProcessorManager manager = tempMap.get(config.uriPath());
					if( manager == null ){						
						manager = new HttpProcessorManager(config.contentType(), clazz, processorPoolSize);						
						if( StringUtil.isNotEmpty(config.uriPath()) ){
							tempMap.put(config.uriPath(), manager);
							String s = clazz.getName() + " ;uriPath:"+config.uriPath()+" ;content-type:"+config.contentType();
							System.out.println("... registered "+accessMode+" controller "+ s);
							HttpProcessorManager.setUriMappingText(accessMode+" "+config.uriPath(), s);
						} 
						else if( StringUtil.isNotEmpty(config.uriPattern()) ){
							tempMap.put(config.uriPattern(), manager);
							String s = clazz.getName() + " ;uriPattern:"+config.uriPattern() + " ;content-type:"+config.contentType();
							System.out.println("... registered "+accessMode+" controller "+ s);
							HttpProcessorManager.setUriMappingText(accessMode+" "+"*/"+config.uriPattern()+"/*", s);
						}
						else {
							throw new IllegalArgumentException("the class "+clazz.getName() + " is missing uriPath or uriPattern config");
						}    				
					} else {
						throw new IllegalArgumentException("duplicated "+ config.uriPath() + " , existed class " + manager.getClass().getName());
					}
				}
			}  	        	
	    }
	    return Collections.unmodifiableMap(tempMap);
	}
	
	/**
	 * routing to processor, matched by exact URI. E.g: http://example.com/get-data?id=1 will match URI "/get-data"
	 * 
	 * @param handlers
	 * @param qDecoder
	 * @return
	 */
	public static final HttpProcessorManager routingForUriPath(Map<String, HttpProcessorManager> handlers, QueryStringDecoder qDecoder){		
		return handlers.get(qDecoder.path());
	}
	
	/**
	 * routing to processor, matched by pattern. E.g: http://example.com/v1/get-data/id_1 will match pattern "get-data" 
	 * 
	 * @param handlers
	 * @param qDecoder
	 * @param index
	 * @return
	 */
	public static final HttpProcessorManager routingForUriPattern(Map<String, HttpProcessorManager> handlers, QueryStringDecoder qDecoder, int index){
		String[] toks = qDecoder.path().split("/");
		if(toks.length > index){
			String pathPattern = toks[index];
			return handlers.get(pathPattern);						
		}
		return null;
	}
}