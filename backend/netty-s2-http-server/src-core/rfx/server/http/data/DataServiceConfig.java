package rfx.server.http.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataServiceConfig {
	String template() default "system/default";
	int type() default HANDLEBARS_TEMPLATE; //default is handlebars template engine
	boolean autoUnescapeHtml() default true; //HTML decode 	result from template output
	
	/*
	 * marking the service is reactive, that means:
	 *  auto logging request to Kafka and Rfx-Stream Analytics for statistics and analyzing 
	 *  after analyzing, the new state will be push back to client via HTTP WebSocket
	 */
	boolean reactive() default false;
	
	public static int STATIC_FILE = 11;
	public static int HANDLEBARS_TEMPLATE = 0;
	public static int MUSTACHE_TEMPLATE = 1;
}