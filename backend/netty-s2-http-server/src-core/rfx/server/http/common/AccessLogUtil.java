package rfx.server.http.common;

import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import rfx.server.configs.HttpServerConfigs;

public class AccessLogUtil {
    private static final Logger accessLog = Logger.getLogger("s2-http-log-server.accessLog");
    private static final String DEFAULT_LOG_FORMAT =
            "%1$s - - [%2$td/%2$tb/%2$tY:%2$tT %2$tz] \"%3$s %4$s %5$s\" %6$d %7$d";
    
    
    private static int isEnable = 0;
    public static void logAccess(HttpRequest request, String inetAddress, String uri) {
        // We've got to use the a default locale here, so that month name is
        // formatted properly for CLF, regardless of server locale. I've chosen Canada!
        if (isEnable == 1 && accessLog.isInfoEnabled()) {
        	int status = 200;
        	int contentLength = uri.length();
        	String method = request.getMethod().name();
        	String protocol = request.getProtocolVersion().protocolName();
            accessLog.info(String.format(Locale.CANADA, DEFAULT_LOG_FORMAT,
                inetAddress, Calendar.getInstance(), method, uri, protocol, status,
                contentLength));
            System.out.println(inetAddress);
        }
        
    }
    
    public static void configureAccessLog(HttpServerConfigs configs) {
    	String fileName = configs.getAccessLogFileName();
    	isEnable = configs.getAccessLogEnable();
    	if(isEnable == 1){
    		accessLog.setLevel(org.apache.log4j.Level.INFO);
            try {
                Layout layout = new PatternLayout("%m%n");
                FileAppender fileAppender = new FileAppender(layout, fileName);
                accessLog.addAppender(fileAppender);
                accessLog.setAdditivity(false);
            }
            catch (IOException e) {
            	e.printStackTrace();
            }	
    	}
        
    }


}
