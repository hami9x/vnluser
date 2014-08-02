package rfx.server.util.memcache;


import java.io.IOException;

import net.spy.memcached.MemcachedClient;

/**
 * the utility class for connecting and retrieving data from MemCached
 * 
 * @author Trieu.nguyen
 *
 * @param <T>
 */
public abstract class MemcacheCommand<T> {
	protected MemcachedClient mcClient;
	protected String poolname;
	
	public MemcacheCommand(String poolname) throws IOException {
		this.poolname = poolname;
		mcClient = MemcacheUtil.getMemcachedClient(poolname);
	}
	
	public T execute() {
		return execute(false);
	}
	
	public T execute(boolean autoClose) {		
		T rs = null;
		try {			
			if (mcClient != null) {
				rs = build();
				if(autoClose){
					MemcacheUtil.freeMemcachedResource(poolname);
				}
			}
		} catch (Exception e) {
			if(e instanceof java.lang.IllegalStateException){
				MemcacheUtil.freeMemcachedResource(poolname);
			} else {
				e.printStackTrace();	
			}		
		}		
		return rs;
	}
	
	//define the logic at implementer
	protected abstract T build();
}
