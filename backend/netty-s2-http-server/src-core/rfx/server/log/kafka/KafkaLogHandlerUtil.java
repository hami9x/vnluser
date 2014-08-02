package rfx.server.log.kafka;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rfx.server.http.common.CookieUtil;
import rfx.server.http.common.NettyHttpUtil;
import rfx.server.log.handlers.StaticFileHandler;
import rfx.server.util.StringUtil;

public class KafkaLogHandlerUtil {
	public static FullHttpResponse webLogHandler(String ipAddress, HttpRequest request, String uri, String kafkaType) {		
		FullHttpResponse response = StaticFileHandler.theBase64Image1pxGif();
		if(NettyHttpUtil.isBadLogRequest(uri)){
			return response;
		}
		KafkaLogHandler kafkaHandler = HttpLogKafkaHandler.getKafkaHandler(kafkaType);
		if(kafkaHandler != null){
			Cookie userid = CookieUtil.getAnomyousCookie(request);
			if (userid != null) {
				kafkaHandler.writeLogToKafka(ipAddress, request);
				CookieUtil.setAnomyousCookie(userid, response);
			}
			else { // browser not accept cross-domain cookies
				kafkaHandler.writeLogToKafka(ipAddress, request);
			}		
			//RedisManagerUtil.logCounter(kafkaType);
			//RedisManagerUtil.increaseIpAddressAndHttpPath(ipAddress, kafkaType);
		}		
		return response;
	}
	
	public static FullHttpResponse mobileLogHandler(String ipAddress, HttpRequest request, String uri, String kafkaType) {		
		QueryStringDecoder qdecoder = new QueryStringDecoder(uri);
		Map<String, List<String>> params = qdecoder.parameters();
		String callbackFunc = NettyHttpUtil.getParamValue("callback", params, "");
		
		Map<String, Object> data = new HashMap<>(2);		
		if(NettyHttpUtil.isBadLogRequest(uri)){
			data.put("status", "fail");
			data.put("msg", "is bad log request");			
			return StaticFileHandler.theJSONContent(NettyHttpUtil.responseAsJsonp(callbackFunc, data));
		}
		
		KafkaLogHandler kafkaHandler = HttpLogKafkaHandler.getKafkaHandler(kafkaType);
		if(kafkaHandler != null){
		   kafkaHandler.writeMobileLogToKafka(ipAddress, request);
		}
		data.put("status", "ok");
		data.put("msg", "success");
		
		FullHttpResponse response = StaticFileHandler.theJavaScriptContent(NettyHttpUtil.responseAsJsonp(callbackFunc, data));
		String headerOrigin = request.headers().get("Origin");
		if(StringUtil.isNotEmpty(headerOrigin)){
			response.headers().set("Access-Control-Allow-Origin",headerOrigin);
			response.headers().set("Access-Control-Allow-Methods","GET");
			response.headers().set("Access-Control-Allow-Credentials","true");				
		}
		return  response;
	}
}
