package rfx.server.http;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rfx.server.http.common.NettyHttpUtil;
import rfx.server.http.data.HttpRequestEvent;

/**
 * the public handler for all Netty's message, transform to HttpRequestEvent and routing all matched processors
 * 
 * @author trieu
 *
 */
public class PublicHttpProcessorRoutingHandler extends SimpleChannelInboundHandler<Object> {
	
	private static final Map<String, HttpProcessorManager> handlers = new HashMap<>();
	
	//TODO move to config file
	final static String MAIN_PACKAGE = "ambient.delivery";
	public static final int PATTERN_INDEX = 2;
	public static int DEFAULT_MAX_POOL_SIZE = 20000;
		
	public PublicHttpProcessorRoutingHandler(){}
	
	public static void init(String mainPackage, int processorPoolSize) throws Exception{
		handlers.putAll(HttpProcessorManager.initProcessorPool(mainPackage, HttpProcessorConfig.PUBLIC_ACCESS, processorPoolSize));
	}
	
	public static void init(String mainPackage) throws Exception{
		init(mainPackage, DEFAULT_MAX_POOL_SIZE);
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {    	
        if (msg instanceof HttpRequest) {        	
        	HttpRequest request = (HttpRequest) msg;
        	//TODO filter DDOS/bad/attacking requests 
        	        	
        	String uri = request.getUri();
        	String remoteIp = NettyHttpUtil.getRemoteIP(ctx, request);
        	String localIp = NettyHttpUtil.getLocalIP(ctx);
        	        	
        	System.out.println(request.getMethod().name() + "==> uri: " + uri);
        	

    		if (uri.equalsIgnoreCase(NettyHttpUtil.FAVICON_URI)) {
    			NettyHttpUtil.response1pxGifImage(ctx);
    		} else {
    			FullHttpResponse response = null;
				
				QueryStringDecoder qDecoder = new QueryStringDecoder(uri);
				Map<String, List<String>> params = qDecoder.parameters();
				
				//boolean isPOSTMethod = "POST".equals(request.getMethod().name());
				
				HttpProcessorManager processorManager = HttpProcessorManager.routingForUriPath(handlers,qDecoder);
				HttpRequestEvent requestEvent = null;
				if(processorManager != null){
					requestEvent = new HttpRequestEvent(localIp, remoteIp, uri, params, request);
					response = processorManager.doProcessing(requestEvent);
				} else {
					processorManager = HttpProcessorManager.routingForUriPattern(handlers,qDecoder, PATTERN_INDEX);
					if(processorManager != null){
						requestEvent = new HttpRequestEvent(localIp, remoteIp, uri, params, request);
						response = processorManager.doProcessing(requestEvent);
					} else {
						String s = "Not found HttpProcessor for URI: "+uri;
						response = NettyHttpUtil.theHttpContent(s, HttpResponseStatus.NOT_FOUND);
					}
				}
				
				//set version to response header
				response.headers().add("Server", HttpServer.SERVER_INFO_VERSION);
				
				// Write the response.				
		        ChannelFuture future = ctx.write(response);
		        ctx.flush().close();
		        
		        //callback and free resources 
		        if(requestEvent != null){
		        	requestEvent.clear();
		        }
				 
				//Close the non-keep-alive connection after the write operation is done.
				future.addListener(ChannelFutureListener.CLOSE);
    		}
        }

        if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {                
            	NettyHttpUtil.response1pxGifImage(ctx);
            }
        }
    }    
   
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    	ctx.flush();
    }       
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	ctx.flush().close(); 
    }
    
    @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.flush().close();   
	}
}