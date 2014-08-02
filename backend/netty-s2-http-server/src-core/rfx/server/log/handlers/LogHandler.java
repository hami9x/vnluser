package rfx.server.log.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public interface LogHandler {
	public FullHttpResponse handle(ChannelHandlerContext ctx, HttpRequest request, String uri, String ipAddress);
}
