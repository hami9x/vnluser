package rfx.server.http.common;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import rfx.server.http.PrivateHttpProcessorRoutingHandler;

public class PrivateHttpServerInitializer extends ChannelInitializer<SocketChannel> {
	
		
	public PrivateHttpServerInitializer(String mainPackage, int processorPoolSize) throws Exception {
		PrivateHttpProcessorRoutingHandler.init(mainPackage, processorPoolSize);
	}
	
	public PrivateHttpServerInitializer(String mainPackage) throws Exception {
		PrivateHttpProcessorRoutingHandler.init(mainPackage);
	}
		

	@Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));
        //TODO support SSL HTTP

        p.addLast("decoder", new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        //p.addLast("aggregator", new HttpObjectAggregator(1048576));
        p.addLast("encoder", new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast("deflater", new HttpContentCompressor());
        p.addLast("handler", new PrivateHttpProcessorRoutingHandler());         
    }
}