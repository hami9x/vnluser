package rfx.server.http.common;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.REFERER;
import static io.netty.handler.codec.http.HttpHeaders.Names.USER_AGENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpOutputResource;
import rfx.server.log.handlers.StaticFileHandler;
import rfx.server.util.CharPool;
import rfx.server.util.LogUtil;
import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;

import com.google.gson.Gson;

public class NettyHttpUtil {
	public static final String FAVICON_URI = "/favicon.ico";
	public static final String HEADER_REFERER_NAME = "Referer";
	public static final String HEADER_REFRESH_NAME = "Refresh";
	public static final String HEADER_LOCATION_NAME = "Location";	
	public static final String HEADER_CONNECTION_CLOSE = "Close";
	public static final String[] REFERER_SEARCH_LIST = new String[]{"\t%s","\t","%s","\r\n","\n","\r"};
	public static final String[] REFERER_REPLACE_LIST = new String[]{"","","","","",""};
	
	//redirect to url using HTML+JavaScript to preserve the referer, solution at https://coderwall.com/p/7a09ja
	static final String HTML_FOR_REDIRECT;
	static {
		StringBuilder s= new StringBuilder();
		s.append("<!DOCTYPE html><html><head><title></title></head><body>");
		s.append("<script type='text/javascript' >window.location=\"$url\";</script>");
		//tracking when javascript can not redirect to targeted url
		//s.append("<noscript><img src='http://localhost:8080/ar?redirect=$autourl' /></noscript>");
		s.append("</body></html>");		
		HTML_FOR_REDIRECT = s.toString();
	} 
	

	public static FullHttpResponse redirectPath(String uri)
			throws UnsupportedEncodingException {
		int i = uri.indexOf("/http");
		if (i > 0) {
			// String metaUri = uri.substring(0, i);
			// do something with metaUri, E.g:
			// /r/13083/142/zizgzlzmzqzlzizhzizrzoziznzhzozizgzjzrzgzozizizgzdzizlzhzdzizkzmzdzmzgzozjzm21zjzmzq1t1u1t20201v21zjzjzr

			String url = uri.substring(i + 1);
			// System.out.println(metaUri + " " + url) ;
			return redirect(URLDecoder.decode(url, StringPool.UTF_8));
		}
		return null;
	}
	
	public static FullHttpResponse redirect(String url) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,HttpResponseStatus.MOVED_PERMANENTLY);
		response.headers().set(HEADER_LOCATION_NAME, url);	
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
	
	/**
	 * redirect url and preserve the referer in header
	 * 
	 * @param url
	 * @return FullHttpResponse
	 */
	public static FullHttpResponse redirectWithReferer(String url) {			
		String html = HTML_FOR_REDIRECT.replace("$url", url);
		ByteBuf byteBuf = Unpooled.copiedBuffer(html.getBytes());		
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,HttpResponseStatus.OK, byteBuf);		
		response.headers().set(HEADER_LOCATION_NAME, url);
		response.headers().set(CONTENT_TYPE, ContentTypePool.HTML_UTF8);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}


	public static FullHttpResponse theHttpContent(String str) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK ,byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.TEXT_UTF8);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
	
	public static FullHttpResponse theHttpContent(String str, String contentType) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());	
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK ,byteBuf);
		response.headers().set(CONTENT_TYPE, contentType);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);		
		return response;
	}
	
	public static FullHttpResponse theHttpContent(HttpOutputResource re, String contentType) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK , re.getByteBuf());
		response.headers().set(CONTENT_TYPE, contentType);
		response.headers().set(CONTENT_LENGTH, re.getLength());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);		
		//System.out.println("CONTENT_LENGTH:"+re.getLength());
		//System.out.println();
		return response;
	}
	
	public static FullHttpResponse theHttpContent(String str, HttpResponseStatus status) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status ,byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.TEXT_UTF8);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}

	public static String getParamValue(String name, Map<String, List<String>> params) {
		return getParamValue(name, params, StringPool.BLANK);
	}
	
	public static String getParamValue(String name, Map<String, List<String>> params, String defaultVal) {
		List<String> vals = params.get(name);
		if (vals != null) {
			if (vals.size()>0) {
				return vals.get(0);
			}
		}
		return defaultVal;
	}
	
	public static String getRemoteIP(ChannelHandlerContext ctx) {
		try {
			SocketAddress address = ctx.channel().remoteAddress();
			if(address instanceof InetSocketAddress){
				return ((InetSocketAddress)address).getAddress().getHostAddress();
			}
			return address.toString().split("/")[1].split(":")[0];
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "0.0.0.0";
	}
	
	public static String getLocalIP(ChannelHandlerContext ctx) {
		try {
			SocketAddress address = ctx.channel().localAddress();
			if(address instanceof InetSocketAddress){
				return ((InetSocketAddress)address).getAddress().getHostAddress();
			}
			return address.toString().split("/")[1].split(":")[0];
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "0.0.0.0";
	}
	
	static final String unknown = "unknown" ;
	//http://r.va.gg/2011/07/handling-x-forwarded-for-in-java-and-tomcat.html
	public static String getRemoteIP(ChannelHandlerContext ctx, HttpRequest request){
		String ipAddress = request.headers().get("X-Forwarded-For");		
		if ( ! StringUtil.isNullOrEmpty(ipAddress) && ! unknown.equalsIgnoreCase(ipAddress)) {			
			//LogUtil.dumpToFileIpLog(ipAddress);
			String[] toks = ipAddress.split(",");
			int len = toks.length;
			if(len > 1){
				ipAddress = toks[len-1];
			} else {				
				return ipAddress;
			}
		} else {		
			ipAddress = NettyHttpUtil.getRemoteIP(ctx);
		}		
		return ipAddress;
	}
	
	/**
	 * quick log data for error (video tracking)
	 * 
	 * @param ipAddress
	 * @param request
	 * @param uri
	 */
	public static void logErrorData(String ipAddress, HttpRequest request,String uri){		
		if(StringUtil.isEmpty(uri)){
			return;
		}
		int idx = uri.indexOf("?");
		if(idx < 0){
			return;
		}
		String queryDetails = uri.substring(idx+1);
		if(StringUtil.isEmpty(queryDetails)){
			return;
		}		
		try {
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
			Map<String, List<String>> params = queryStringDecoder.parameters();
			String error = NettyHttpUtil.getParamValue("error", params, "");

			if( ! StringUtil.isEmpty(error) ){
				String userAgent = request.headers().get(USER_AGENT);
				String cookieString = request.headers().get(COOKIE);
				long time = System.currentTimeMillis() / 1000L;
				
				StringBuilder logLine = new StringBuilder();			
				char tab = CharPool.TAB;
				logLine.append(ipAddress).append(tab);
				logLine.append(time).append(tab);
				logLine.append(userAgent).append(tab);
				logLine.append(queryDetails).append(tab);
				logLine.append(cookieString);
				logLine.append("\n");
				
				LogUtil.dumpErrorLogData(logLine.toString());
			}
		} catch (Exception e) {}		
	}
	
	public static boolean isBadLogRequest(String uri){
		if(StringUtil.isEmpty(uri)){
			return true;
		}
		int idx = uri.indexOf("?");
		if(idx < 0){
			return true;
		}
		String queryDetails = uri.substring(idx+1);
		if(StringUtil.isEmpty(queryDetails)){
			return true;
		}
		return false;
	}
	
	//TODO
//	public static boolean filterLogsByDomain(String uri){
//		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
//		Map<String, List<String>> params = queryStringDecoder.parameters();
//		String origin = NettyHttpUtil.getParamValue("origin", params);
//		LogFilterConfigs logFilterConfigs = LogFilterConfigs.load();
//		Map<String, String> domains = logFilterConfigs.getOnlyWriteForDomains();
//		Set<String> keys = domains.keySet();
//		for (String key : keys) {
//			return origin.contains(key);
//		}
//		return false;
//	}
	
	public static String responseAsJsonp(String callbackFunc, Map<String, Object> data){
		String jsonData = new Gson().toJson(data);
		return responseAsJsonp(callbackFunc, jsonData);
	}
	
	public static String responseAsJsonp(String callbackFunc, String jsonData){		
		if( StringUtil.isEmpty(callbackFunc) ){			
			return jsonData;
		} else {
			StringBuilder jsonp = new StringBuilder(callbackFunc);
			jsonp.append("(").append(jsonData).append(")");
			return jsonp.toString();
		}	
	}
	
	public static void sendHttpResponse(ChannelHandlerContext ctx,	FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			setContentLength(res, res.content().readableBytes());
		}
		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public static void response1pxGifImage(ChannelHandlerContext ctx) {
		FullHttpResponse response = StaticFileHandler.theBase64Image1pxGif();
	    ChannelFuture future = ctx.write(response);
	    ctx.flush();
	    ctx.close();			 
		//Close the non-keep-alive connection after the write operation is done.
		future.addListener(ChannelFutureListener.CLOSE);	     
	}
	
	public static String getRefererUrl(HttpHeaders headers){
		String refererUrl = headers.get(REFERER);
		if(StringUtil.isNotEmpty(refererUrl)){
			refererUrl = StringUtils.replaceEach(refererUrl, REFERER_SEARCH_LIST,  REFERER_REPLACE_LIST);
		}
		return refererUrl;
	}
}
