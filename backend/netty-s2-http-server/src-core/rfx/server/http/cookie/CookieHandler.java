package rfx.server.http.cookie;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;


public class CookieHandler {
	public CookieHandler(){
		
	}
	public void cookieSync(HttpRequest request, FullHttpResponse response) {
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		Map<String, List<String>> params = queryStringDecoder.parameters();
		String nid = "";
		String callbackname = "";
		String anomynousid = "";
	
		if (params.get("nid").size() > 0) {
			nid = params.get("nid").get(0);
		}
		
		if (params.get("callback").size() > 0) {
			callbackname = params.get("callback").get(0);
		}
		
		StringBuilder buf = new StringBuilder();
		
		buf.append(String.format("%s({\"result\":%s,\"nid\":\"%s\",\"vid\":\"%s\"});", callbackname, nid, anomynousid).getBytes());
		
		
		response = new DefaultFullHttpResponse(HTTP_1_1,OK,
				Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
		
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
	}
	public void cookieProcess(HttpRequest request, FullHttpResponse response) {
//		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
//		Map<String, List<String>> params = queryStringDecoder.parameters();
	}

}
