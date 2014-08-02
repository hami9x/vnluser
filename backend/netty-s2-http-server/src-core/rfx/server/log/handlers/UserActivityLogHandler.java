package rfx.server.log.handlers;

import rfx.server.log.kafka.HttpLogKafkaHandler;
import rfx.server.log.kafka.KafkaLogHandlerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public class UserActivityLogHandler  implements LogHandler {

	static String trackingPath = "/tracking";
	
	@Override
	public FullHttpResponse handle(ChannelHandlerContext ctx, HttpRequest request, String uri, String ipAddress){	
		if (uri.startsWith(trackingPath)) {
			System.out.println("uri:"+uri);
			return KafkaLogHandlerUtil.webLogHandler(ipAddress, request, uri, HttpLogKafkaHandler.logSocialActivityKafka);
		}
		return null;
	}
	
}
