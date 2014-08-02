package rfx.server.http;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import rfx.server.configs.HttpServerConfigs;
import rfx.server.http.common.AccessLogUtil;

/**
 * HTTP Log server.
 * based on https://github.com/netty/netty/tree/master/example/src/main/java/io/netty/example/http/snoop
 */
public class HttpServerStarter {
   
	//-Xms256m -Xmx2048m -XX:MaxNewSize=512m
    public static void main(String[] args) throws Exception {
    	
    	CommandLineParser parser = new PosixParser();
		// create the Options
		Options options = new Options();		
		options.addOption( "d", "debug", false, "Enable server debug-mode" );
		options.addOption( "c", "cached", false, "Auto-caching all things: templates, server-configs in local Java Memory");
		options.addOption("lcf","load-configs-from", true, "load from specified config-folder name");

		if(args.length == 0){
			args = new String[]{ "-d" , "--load-configs-from=configs/" };	
		}

		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp( "./start-server", options );

		    // validate that block-size has been set				   
		    if(line.hasOption("load-configs-from")){
		    	System.out.println( line.getOptionValue( "load-configs-from" ) );
		    } else {
		    	System.out.println( "use default configs" );
		    }
		    
		    if(line.hasOption("cached")){
		    	System.out.println("cached");
		    } else {
		    	System.out.println("no cached");
		    }
		    
		    if(line.hasOption("debug")){
		    	System.out.println("debug");
		    } else {
		    	System.out.println("no debug");
		    }
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
    	
    	
    	HttpServerConfigs configs = HttpServerConfigs.load();
    	int customPort = 0;
    	boolean websocket = false;
//    	if(args.length >= 1){
//    		configs = HttpServerConfigs.load(args[0]);    		
//    	} else {
//    		configs = HttpServerConfigs.load();
//    	}    	
    	websocket = configs.isWebsocketEnable();
    	
        int port = configs.getPort();
        if (customPort != 0 ) {
        	port = customPort;
        }
        String ip = configs.getIp();
        //HttpLogKafkaHandler.initKafkaSession();//SKIP in the first version 1.0
        AccessLogUtil.configureAccessLog(configs);
        
        System.out.println("-------------- HTTP SERVER LOG ["+ip+":"+port+"] --------------");
        if(websocket){
        	System.out.println(" #############  WebsocketEnabled Mode  #############");
        } else {
        	System.out.println(" #############  Http Server Enabled Mode  #############");
        }
        //MemoryManagementUtil.startMemoryUsageTask();
        String publicClasspath = HttpServer.DEFAULT_CLASSPATH;
        new HttpServer(ip,port).run(websocket,publicClasspath);
    }
}
