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

import java.util.List;
import java.util.Map;

import rfx.server.http.common.NettyHttpUtil;
import rfx.server.http.data.HttpRequestEvent;


/**
 * the private handler for all Netty's message, transform to HttpRequestEvent and routing all matched processors
 * 
 * @author trieu
 */
public class PrivateHttpProcessorRoutingHandler extends SimpleChannelInboundHandler<Object> {
	
	private static Map<String, HttpProcessorManager> handlers;
	
	public static final int PATTERN_INDEX = 2;
	public static int DEFAULT_MAX_POOL_SIZE = 20;
		
	public PrivateHttpProcessorRoutingHandler(){}
	
	public static void init(String classpath, int processorPoolSize) throws Exception{
		handlers = HttpProcessorManager.initProcessorPool(classpath, HttpProcessorConfig.PRIVATE_ACCESS, processorPoolSize);
	}
	
	public static void init(String classpath) throws Exception{
		init(classpath, DEFAULT_MAX_POOL_SIZE);
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {    	
        if (msg instanceof HttpRequest) {        	
        	HttpRequest request = (HttpRequest) msg;
        	String uri = request.getUri();
        	String remoteIp = NettyHttpUtil.getRemoteIP(ctx, request);
        	String localIp = NettyHttpUtil.getLocalIP(ctx);
        
    		if (uri.equalsIgnoreCase(NettyHttpUtil.FAVICON_URI)) {
    			NettyHttpUtil.response1pxGifImage(ctx);
    		} else {
    			FullHttpResponse response = null;
    			//response = NettyHttpUtil.theHttpContent("Delivery Server OK");
    			
    			
    			//TODO access log
//				try {
//					AccessLogUtil.logAccess(request, ipAddress, uri);
//					response = UriMapper.buildHttpResponse(ipAddress,ctx,request , uri);
//				} catch (Exception e) {
//					e.printStackTrace();
//					LogUtil.error("HttpLogChannelHandler", e.getMessage());
//				}
				
				QueryStringDecoder qDecoder = new QueryStringDecoder(uri);
				Map<String, List<String>> params = qDecoder.parameters();
				
				HttpProcessorManager processorManager = HttpProcessorManager.routingForUriPath(handlers,qDecoder);
				HttpRequestEvent requestEvent = null;
				if(processorManager != null){
					requestEvent = new HttpRequestEvent(localIp,remoteIp, uri, params, request);
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
//		        if(requestEvent != null){
//		        	requestEvent.clear();
//		        }
				 
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
