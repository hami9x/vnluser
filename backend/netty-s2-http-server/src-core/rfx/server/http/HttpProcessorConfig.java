package rfx.server.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import rfx.server.configs.ContentTypePool;
import rfx.server.util.StringPool;

@Target(ElementType.TYPE) //on class level
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpProcessorConfig {	 
	String uriPath() default StringPool.BLANK; 
	String uriPattern() default StringPool.BLANK; 
	String contentType() default ContentTypePool.TEXT_UTF8;
	int privateAccess() default 0;
	
	public static int PUBLIC_ACCESS = 0;
	public static int PRIVATE_ACCESS = 1;
}