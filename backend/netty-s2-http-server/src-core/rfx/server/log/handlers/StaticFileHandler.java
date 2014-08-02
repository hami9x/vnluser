package rfx.server.log.handlers;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.IOException;

import rfx.server.configs.ContentTypePool;
import rfx.server.configs.HttpServerConfigs;
import rfx.server.util.FileUtils;
import rfx.server.util.StringPool;

public class StaticFileHandler {
	@Deprecated
	static final String staticCrossdomainFile = "./resources/static/crossdomain.xml";
	
	static final String HTTP_HEADER_CACHE = "must_revalidate, private, max-age=";
	static final String HEADER_CONNECTION_CLOSE = "Close";
		
	static final byte[] BASE64GIF_BYTES = StringPool.BASE64_GIF_BLANK.getBytes();
	
	@Deprecated
	static byte[] CROSSDOMAINXML_BYTES;
	
	static int cacheHttpMaxAge = 7200;
	static String httpHeaderCache = "";
	
	static {
		try {
			CROSSDOMAINXML_BYTES = FileUtils.loadFilePathToString(staticCrossdomainFile).getBytes();
			cacheHttpMaxAge = HttpServerConfigs.load().getCacheHttpMaxAge();
			httpHeaderCache = HTTP_HEADER_CACHE + cacheHttpMaxAge;
		} catch (IOException e) {			
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	public static FullHttpResponse theBase64Image1pxGif() {
		ByteBuf byteBuf = Base64.decode(Unpooled.copiedBuffer(BASE64GIF_BYTES));
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK , byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.GIF);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
	
	@Deprecated
	public static FullHttpResponse staticCrossdomainFileContent() {
		ByteBuf byteBuf = Unpooled.copiedBuffer(CROSSDOMAINXML_BYTES);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK ,byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.XML);
		response.headers().set("cache-control",httpHeaderCache);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
	
	public static FullHttpResponse theJavaScriptContent(String str) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK ,byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.JAVA_SCRIPT);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
	
	public static FullHttpResponse theJSONContent(String str) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(str.getBytes());
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK ,byteBuf);
		response.headers().set(CONTENT_TYPE, ContentTypePool.JSON);
		response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
		response.headers().set(CONNECTION, HEADER_CONNECTION_CLOSE);
		return response;
	}
}
