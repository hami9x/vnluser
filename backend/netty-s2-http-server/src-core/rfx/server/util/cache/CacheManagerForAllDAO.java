package rfx.server.util.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.reflections.Reflections;

import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;

import com.google.common.cache.LoadingCache;

/**
 * How to use Spring AOP http://www.journaldev.com/2583/spring-aop-example-tutorial-aspect-advice-pointcut-joinpoint-annotations-xml-configuration
 * 
 * @author Trieu.nguyen
 *
 */
@Aspect
public class CacheManagerForAllDAO {
	
	final static String daoClasspath = "sample.pollapp.business.dao";
	final static String withinClasspath = "within("+daoClasspath+".*)";
	
	final static Map<String, CachePool> signatureConfigCache = new HashMap<>();
	final static Map<String, Boolean> globalCachableMethods = new HashMap<>();

	//TODO use Memcache here
	static boolean cacheAllMethodsInDAO = true;
	
	public CacheManagerForAllDAO() {
		System.out.println("---CacheManagerForAllDAO---");
	}
		
	@Around(withinClasspath)
    public Object process(ProceedingJoinPoint pJoinPoint){
		try {
			String className = pJoinPoint.getTarget().getClass().getName();
			CachePool cachePool = signatureConfigCache.get(className);
			if(cachePool != null){
				Object value = null;
//		        System.out.println(" ---------Before invoking ---------- ");
		        
		        //String key = pJoinPoint.getSignature().getName() + HashUtil.hashUrlCrc64(Arrays.toString(pJoinPoint.getArgs()));
			
				Signature method = pJoinPoint.getSignature();
				String methodName = method.getName();
				Object[] args = pJoinPoint.getArgs();
				long expireAfter = cachePool.getExpireAfter(methodName);
				if(expireAfter > 0){
					System.out.println(className +" " +methodName + " " + cachePool);
					String key = cachePool.buildKey(methodName, args);
			        LoadingCache<String, Object> cache = cachePool.getCache();
		        	value = cache.get(key);	     
		        	
		        	System.out.println("++ Target: "+pJoinPoint.getTarget().getClass().getName());
		        	System.out.println("++ Signature: "+pJoinPoint.getSignature().getName());
		        	
		        	System.out.println("++ call method=" + pJoinPoint.getSignature().getName());
	        		System.out.println("++ Agruments Passed=" + Arrays.toString(pJoinPoint.getArgs()));
		        	
		        	if(StringUtil.isEmpty(value)){	        		
		                value = pJoinPoint.proceed();	                
		                cache.put(key, value);
		        	} else {
		        		System.out.println("Hit cache by key: " + key );
		        	}		
				} else {
					value = pJoinPoint.proceed();
				}				
	        	//System.out.println(" value: "+value);
	        	
		        return value;	
			}
			return pJoinPoint.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}    
		return null;
    }

	@Before(withinClasspath)
	public void logStringArguments(JoinPoint joinPoint) {
		System.out.println("Before call method= " + joinPoint.toString());
		//System.out.println("Agruments Passed=" + Arrays.toString(joinPoint.getArgs()));

	}
	
	static class CachePool {
		LoadingCache<String, Object> cache;
		String keyPrefix;
		Map<String, Long> cachableMethods;
		long defaultExpire = 1;
		 		 
		public CachePool(LoadingCache<String, Object> cache, String keyPrefix, Map<String, Long> cachableMethods, long defaultExpire) {
			super();
			this.cache = cache;
			this.keyPrefix = keyPrefix;
			this.cachableMethods = cachableMethods;
			this.defaultExpire = defaultExpire;
		}
		
		public LoadingCache<String, Object> getCache() {
			return cache;
		}
		public void setCache(LoadingCache<String, Object> cache) {
			this.cache = cache;
		}
		public String getKeyPrefix() {
			return keyPrefix;
		}
		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}
		 
		public String buildKey(String signatureName, Object[] args){
			if(StringUtil.isEmpty(keyPrefix)){
				return StringUtil.toString(signatureName, StringPool.UNDERLINE, StringUtil.join(args,StringPool.UNDERLINE));
			}
			return StringUtil.toString(keyPrefix, signatureName, StringPool.UNDERLINE, StringUtil.join(args,StringPool.UNDERLINE));
		}
		
		public long getExpireAfter(String methodName){
			if(cachableMethods != null){
				return cachableMethods.getOrDefault(methodName, 0L);
			}
			return defaultExpire;
		}
		
		@Override
		public String toString() {
			return StringUtil.convertObjectToJson(this);
		}
	}

	public static void init() throws Exception{
		Reflections reflections = new Reflections(daoClasspath);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CacheConfig.class);			
		for (Class<?> clazz : classes) {
			String className = clazz.getName();
			if (clazz.isAnnotationPresent(CacheConfig.class) ) {
				Method[] methods = clazz.getMethods();
				Map<String, Long> cachableMethods = new HashMap<>(methods.length);
				for (Method method : methods) {
					if(method.isAnnotationPresent(Cachable.class)){
						Annotation am = method.getAnnotation(Cachable.class);
						Cachable cachable = (Cachable) am;
						
						String mkey;
						if( cachable.keyFormat().isEmpty() ){
							mkey = method.getName(); 
						} else {
							mkey = cachable.keyFormat();//TODO
						}
						if(cachableMethods.containsKey(mkey)){
							throw new IllegalArgumentException("duplicated cachable method key at class:"+className + " method:" + mkey);
						}
						cachableMethods.put(mkey, cachable.expireAfter());
					}
				}
				Annotation annotation = clazz.getAnnotation(CacheConfig.class);
				CacheConfig cacheConfig = (CacheConfig) annotation;
				
				long maximumSize = cacheConfig.maximumSize() > 0 ? cacheConfig.maximumSize() : 1000000;
				long expireAfter = cacheConfig.expireAfter() > 0 ? cacheConfig.expireAfter() : 10;
				String keyPrefix = cacheConfig.keyPrefix();
				int type = cacheConfig.type();							
				
				if(type == CacheConfig.LOCAL_CACHE_ENGINE){	
					LoadingCache<String, Object> cacheImpl = GuavaCacheUtil.getLoadingCache(className, maximumSize, expireAfter );
					signatureConfigCache.put(className, new CachePool(cacheImpl, keyPrefix, cachableMethods, expireAfter));
				} else if(type == CacheConfig.MEMCACHE_CACHE_ENGINE){
					LoadingCache<String, Object> cacheImpl = new MemcacheLoadingImpl();
					signatureConfigCache.put(className, new CachePool(cacheImpl, keyPrefix, cachableMethods, expireAfter));
				}
				
				System.out.println("...registered signatureConfigCache:" + className);
			}			
		}
	}
	
	public static void main(String[] args) throws Exception {
		init();
		System.out.println(signatureConfigCache.get("sample.pollapp.business.dao.PollAppDAOImpl"));
	}
	
}
