package rfx.server.http.websocket;

import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import rfx.server.http.common.NettyHttpUtil;
import rfx.server.util.FileUtils;
import rfx.server.util.StringUtil;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());
	
	static WebSocketChannelManager wsChannelManager = WebSocketChannelManager.get();
	
	//static final SubscribedChannelManager compactStats = new SubscribedChannelManager(SubscribedChannelManager.CHANNEL_NAME_COMPACT_STATS);
	static Map<String, SubscribedChannelManager> rfQuerySubMap = new HashMap<String, SubscribedChannelManager>();
	static {
		String rfQCompact = StringUtil.join(":", "rfq",SubscribedChannelManager.CHANNEL_COMPACT_STATS);
		rfQuerySubMap.put(rfQCompact, new SubscribedChannelManager(SubscribedChannelManager.CHANNEL_COMPACT_STATS));
	}
		
	private static final String WEBSOCKET_PATH = "/websocket";
	private WebSocketServerHandshaker handshaker;	
	

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}		
		
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {		
		super.channelRegistered(ctx);	
		System.out.println(" channelRegistered "+ctx.channel());	
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {	
		super.channelUnregistered(ctx);
		System.out.println(" channelUnregistered, removeWebSocketChannel:"+ctx.channel());		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {		
		super.channelActive(ctx);
		System.out.println(" channelActive "+ctx.channel());		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {		
		super.channelInactive(ctx);
		System.out.println("channelInactive "+ctx.channel());
		wsChannelManager.removeWebSocketChannel(ctx.channel(),false);
		System.out.println(" channelInactive,channelUnregistered "+ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {		
		if (msg instanceof FullHttpRequest) {			
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}
	
	//////////////////////////////// handlers and utils /////////////////////////////
	
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());			
			wsChannelManager.removeWebSocketChannel(ctx.channel(),false);
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}
		// Send the uppercase string back.
		String request = ((TextWebSocketFrame) frame).text();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format("%s received %s", ctx.channel(), request));
		}
		ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		String uri = req.getUri();
		QueryStringDecoder qdecoder = new QueryStringDecoder(uri);
		Map<String, List<String>> params = qdecoder.parameters();
		System.out.println("WebSocketServerHandler uri: "+uri);
		
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			NettyHttpUtil.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.getMethod() != GET) {
			NettyHttpUtil.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,FORBIDDEN));
			return;
		}

		// Send the demo page and favicon.ico
		if (uri.startsWith("/realtime-monitor")) {
			realtimeViewHandler(ctx, req);
			return;
		} else if ("/favicon.ico".equals(uri)) {			
			NettyHttpUtil.response1pxGifImage(ctx);
			return;
		} else if ("/".equals(uri) || "/jsonp".equalsIgnoreCase(uri)){
			statisticsHandler(uri, ctx, req, params);
			return;
		}		

		// Handshake	
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			if(uri.startsWith("/websocket")){
				//TODO collect params and make a RfQuery 
				String display = NettyHttpUtil.getParamValue("display", params, SubscribedChannelManager.CHANNEL_COMPACT_STATS);
				String network = NettyHttpUtil.getParamValue("network", params, "0");
				String filter = NettyHttpUtil.getParamValue("filter", params, "impression");
				String rfQuery = StringUtil.join(":", "rfq",display,network,filter);
				try {
					handshaker.handshake(ctx.channel(), req);
					//handshaker OK, add channel to list
					wsChannelManager.addChannel(rfQuery, ctx.channel(),ctx.channel());
					System.out.println(" websocketReq = true,channelRegistered "+ctx.channel());					
					return;
				} catch (Exception e) {
					e.printStackTrace();					
				}
			}
			NettyHttpUtil.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,NOT_FOUND));
		}
	}
	
	private static String getWebSocketLocation(FullHttpRequest req) {
		return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
	}
	
	public static void statisticsHandler(String uri, ChannelHandlerContext ctx, FullHttpRequest req, Map<String, List<String>> params){		
		String callbackFunc = NettyHttpUtil.getParamValue("callback", params);		
		
		//build the data from redisSubcribeListener.message
		SubscribedChannelManager compactStats = rfQuerySubMap.get(SubscribedChannelManager.CHANNEL_COMPACT_STATS);
		byte[] data = NettyHttpUtil.responseAsJsonp(callbackFunc, compactStats.getMessage() ).getBytes();
		
		ByteBuf content = Unpooled.copiedBuffer(data);			
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
		String contentType;
		if(callbackFunc.isEmpty()){
			contentType = "application/json";
		} else {
			contentType = "application/x-javascript; charset=utf-8";
		}		
		res.headers().set(CONTENT_TYPE, contentType);
		setContentLength(res, content.readableBytes());
		NettyHttpUtil.sendHttpResponse(ctx, req, res);
	}
	
	void realtimeViewHandler(ChannelHandlerContext ctx, FullHttpRequest req){
		byte[] data = "".getBytes();
		try {
			data = FileUtils.readFileAsString("static/realtime-monitor.html").getBytes();
		} catch (Exception e) {				
			e.printStackTrace();
		}
		ByteBuf content = Unpooled.copiedBuffer(data);
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
		res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
		setContentLength(res, content.readableBytes());
		NettyHttpUtil.sendHttpResponse(ctx, req, res);
	}
	
}