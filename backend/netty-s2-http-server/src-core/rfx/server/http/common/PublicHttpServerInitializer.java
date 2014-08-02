package rfx.server.http.common;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import rfx.server.http.PublicHttpProcessorRoutingHandler;

public class PublicHttpServerInitializer extends ChannelInitializer<SocketChannel> {
	
	ChannelHandler getLogChannelHandler(){
//		System.out.println("-----------------getLogChannelHandler-----------------");
		
		return new PublicHttpProcessorRoutingHandler();
	}

	public PublicHttpServerInitializer(String classpath, int processorPoolSize) throws Exception {
		super();
		PublicHttpProcessorRoutingHandler.init(classpath, processorPoolSize);
	}
	
	public PublicHttpServerInitializer(String classpath) throws Exception {
		super();
		PublicHttpProcessorRoutingHandler.init(classpath);
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
        p.addLast("handler", getLogChannelHandler());         
    }
}