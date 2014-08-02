package rfx.server.util.cache;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

public class MemcacheLoadingImpl implements LoadingCache<String, Object>{
	
	Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object get(String arg0, Callable<? extends Object> arg1)
			throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableMap<String, Object> getAllPresent(Iterable<?> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getIfPresent(Object arg0) {
		// TODO Auto-generated method stub
		return tmp.get(arg0);
	}

	@Override
	public void invalidate(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invalidateAll() {
		// TODO Auto-generated method stub
		tmp.clear();
	}

	@Override
	public void invalidateAll(Iterable<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void put(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		tmp.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return tmp.size();
	}

	@Override
	public CacheStats stats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object apply(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConcurrentMap<String, Object> asMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		return tmp.get(arg0);
	}

	@Override
	public ImmutableMap<String, Object> getAll(Iterable<? extends String> arg0)
			throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getUnchecked(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
