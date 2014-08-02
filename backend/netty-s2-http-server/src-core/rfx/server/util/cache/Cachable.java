package rfx.server.util.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import rfx.server.util.StringPool;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cachable {
	long expireAfter() default 3600; //1 hour
	String keyFormat() default StringPool.BLANK;
}