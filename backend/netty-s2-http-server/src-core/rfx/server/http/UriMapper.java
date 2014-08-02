package rfx.server.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

import rfx.server.configs.HttpServerConfigs;
import rfx.server.configs.LogFilterConfigs;
import rfx.server.http.common.CookieUtil;
import rfx.server.http.common.NettyHttpUtil;
import rfx.server.http.websocket.SubscribedChannelManager;
import rfx.server.log.handlers.LogHandler;
import rfx.server.log.handlers.StaticFileHandler;
import rfx.server.util.StringPool;

public class UriMapper {
	// common
	static final String getIdPath = "/getid";
	static final String staticCrossdomainPath = "/crossdomain.xml";
	
	
	static final String authPath = "/auth";
	static final String jsonpPath = "/jsonp";
	static final String v3Path = "/v3";
	
	static HttpServerConfigs httpServerConfigs = HttpServerConfigs.load();
	static LogFilterConfigs logFilterConfigs = LogFilterConfigs.load();
	
	static boolean wsMode = httpServerConfigs.isWebsocketEnable();
	static SubscribedChannelManager compactStats = null;
	static SubscribedChannelManager fullStats = null;
	static LogHandler logHandler = null;
	static {
		if(wsMode){
			compactStats = new SubscribedChannelManager(SubscribedChannelManager.CHANNEL_COMPACT_STATS);
			fullStats = new SubscribedChannelManager(SubscribedChannelManager.CHANNEL_FULL_STATS );
		}		
		try {			
			String classpath = httpServerConfigs.getDefaultLogHandlerClassPath();
			logHandler =  (LogHandler)Class.forName(classpath).newInstance();
			if(logHandler != null){
				System.out.println("--- Create new instance for logHandler ["+classpath+"]");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public static FullHttpResponse buildHttpResponse(String ipAddress, ChannelHandlerContext ctx, HttpRequest request, String uri) {
		System.out.println("URI: " + uri);

		// filter by bad IP
		Map<String, String> badIpMap = logFilterConfigs.getBadIpMap();
		if (badIpMap.containsKey(ipAddress)) {
			return NettyHttpUtil.theHttpContent(StringPool.BLANK);
		}	

		//handle all log requests first
		FullHttpResponse resp = logHandler.handle(ctx, request, uri, ipAddress);		
		if (resp != null) {
			return resp;
		} else {
			if (uri.startsWith(getIdPath)) {
				//the new user id request
				//System.out.println("getIdPath: " + uri);
				return CookieUtil.handleGetIdPath(request, ipAddress, uri);
			} 			
			else if (uri.startsWith(jsonpPath)) {
				// build the data for real-time analytics in jsonp format
				if(wsMode){
					Map<String, List<String>> params = new QueryStringDecoder(uri).parameters();
					String display = NettyHttpUtil.getParamValue("display", params);
					String msg = "";
					if(display.isEmpty() || display.equalsIgnoreCase("compact")){
						msg = compactStats.getMessage();
					} else if(display.equalsIgnoreCase("full")){
						msg = fullStats.getMessage();
					}
					return jsonpHandler(uri,msg,params);
				}				
			} 
			else if (uri.startsWith(staticCrossdomainPath)) {
				return StaticFileHandler.staticCrossdomainFileContent();
			} 			
		}
		return null;
	}

	static FullHttpResponse jsonpHandler(String uri, String jsonData, Map<String, List<String>> params) {		
		String callbackFunc = NettyHttpUtil.getParamValue("callback", params);
		byte[] data = NettyHttpUtil.responseAsJsonp(callbackFunc, jsonData ).getBytes();		
		ByteBuf content = Unpooled.copiedBuffer(data);
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
		String contentType;
		if (callbackFunc.isEmpty()) {
			contentType = "application/json";
		} else {
			contentType = "application/x-javascript; charset=utf-8";
		}
		res.headers().set(CONTENT_TYPE, contentType);
		return res;
	}

}
