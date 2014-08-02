package rfx.server.util.junit;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

import org.reflections.Reflections;

import rfx.server.util.sql.DbDataAccessObject;

public class GenerateUnitTestUtil {

	public static void main(String[] args) {
		String mainPackage = "ambient.delivery.business.dao";
		Reflections reflections = new Reflections(mainPackage);
		Set<Class<?>> daoClasses = reflections
				.getTypesAnnotatedWith(DbDataAccessObject.class);
		for (Class<?> daoClass : daoClasses) {
			if (daoClass.isAnnotationPresent(DbDataAccessObject.class)) {
				Method[] methods = daoClass.getMethods();
				System.out.println(daoClass.getName());
				for (Method method : methods) {
					if (method.isAnnotationPresent(AutoUnitTestMethod.class)) {
						System.out.println("\t" + method.getName());
						Parameter[] params = method.getParameters();
						for (Parameter p : params) {
							System.out.println("\t\t" + p.getName() + " "
									+ p.getType().getName());
						}
					}
				}
			}
		}
	}
}
