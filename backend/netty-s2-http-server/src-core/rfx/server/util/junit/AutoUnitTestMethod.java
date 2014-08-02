package rfx.server.util.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //on METHOD level
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoUnitTestMethod {	
}
