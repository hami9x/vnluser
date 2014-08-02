package sample.http;

import rfx.server.configs.HttpServerConfigs;
import rfx.server.http.HttpServer;
import rfx.server.util.cache.CacheManagerForAllDAO;

public class SampleHttpServer {

	public static void main(String[] args) throws Exception {
    	HttpServerConfigs configs = HttpServerConfigs.load();
    	int customPort = 0;    	
    	
        int port = configs.getPort();
        if (customPort != 0 ) {
        	port = customPort;
        }
        String ip = configs.getIp();  
        
        System.out.println("-------------- SAMPLE HTTP SERVER with HOST["+ip+":"+port+"] --------------");        
        
        //MemoryManagementUtil.startMemoryUsageTask();
        String publicClasspath = "sample";
        int poolSize = 30000;
        boolean cacheAllCompiledTemplates = true;
        
        CacheManagerForAllDAO.init();
        new HttpServer(ip,port, configs.getPrivatePort(), poolSize,cacheAllCompiledTemplates).run(false,publicClasspath);
	}
}
