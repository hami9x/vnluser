package rfx.server.util.memcache;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.KetamaConnectionFactory;
import net.spy.memcached.MemcachedClient;
import rfx.server.configs.NoSqlServerInfoConfigs;

public class MemcacheUtil {
	
	static Map<String, MemcachedClient> memcachedClientPool = new ConcurrentHashMap<>();
	
	public static MemcachedClient getMemcachedClient(String key) throws IOException {
		MemcachedClient client = memcachedClientPool.get(key);
		if(client == null){
			 String memcache = NoSqlServerInfoConfigs.getServerInfo(key).toString();
	         KetamaConnectionFactory con = new KetamaConnectionFactory();
	         client = new MemcachedClient(con, AddrUtil.getAddresses(memcache));
	         memcachedClientPool.put(key, client);
		}
		return client;
	}
	
	public static void freeMemcachedResource(String key) {
		MemcachedClient client = memcachedClientPool.get(key);
		if(client != null){
			try {
				client.shutdown();
				memcachedClientPool.remove(key);
			} catch (Throwable e) {}
		}		
	}
	
}
