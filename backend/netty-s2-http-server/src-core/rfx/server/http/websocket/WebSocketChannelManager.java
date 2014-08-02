package rfx.server.http.websocket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebSocketChannelManager {
	static Map<String, Map<Object,Channel>> channelsPool = new HashMap<>();
	
	static WebSocketChannelManager _instance; 
	public WebSocketChannelManager() {}
	
	public static WebSocketChannelManager get(String query){
		if(_instance == null){
			_instance = new WebSocketChannelManager();
			channelsPool.put(query, new HashMap<Object,Channel>());
		}
		return _instance;
	}
	
	public static WebSocketChannelManager get(){
		if(_instance == null){
			_instance = new WebSocketChannelManager();
		}
		return _instance;
	}
	
	
	public void addChannel(String query, Object k, Channel channel){
		Map<Object,Channel> channels = channelsPool.get(query);
		if(channels == null ){
			channels = new HashMap<Object,Channel>();
			channelsPool.put(query, channels);
		}		
		channels.put(k,channel);
		System.out.println("channels.size: "+channels.size());
	}
	
	public void removeWebSocketChannel(Object k, boolean forceClose){	
		Collection<Map<Object,Channel>> channelsMap = channelsPool.values();
		for (Map<Object, Channel> channels : channelsMap) {
			Channel c  = channels.remove(k);
			try {			
				if(c != null && forceClose) c.close();
			} catch (Exception e) {}
			finally {
				System.out.println("channels.size: "+channels.size());
			}
		}
	}
	
	public void broadcastMessageToAllChannels(String query, String message){
		System.out.println("channelsPool.size: "+channelsPool.size());
		Map<Object,Channel> channels = channelsPool.get(query);
		if(channels != null ){
			Set<Object> keys = channels.keySet();
			for (Object key : keys) {
				Channel webSocketChannel = channels.get(key);
				//System.out.println(webSocketChannel.toString());
				if(webSocketChannel != null){
					webSocketChannel.write(new TextWebSocketFrame(message));
					webSocketChannel.flush();
				}
			}
		}
	}
}
