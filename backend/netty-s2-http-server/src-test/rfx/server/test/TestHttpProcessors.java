package rfx.server.test;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

import rfx.server.http.HttpProcessorConfig;

public class TestHttpProcessors {
	final static String BASE_CONTROLLER_PACKAGE = "rfx.server.http.processor";

	public static Set<Class<?>> getClasses() {
		final HashSet<Class<?>> classes = new HashSet<Class<?>>();
		// register root resource
		Reflections reflections = new Reflections(BASE_CONTROLLER_PACKAGE);
		Set<Class<?>> clazzes = reflections
				.getTypesAnnotatedWith(HttpProcessorConfig.class);
		for (Class<?> clazz : clazzes) {
			if (!classes.contains(clazz)) {
				classes.add(clazz);
				System.out.println("...registered controller class: " + clazz);
				if (clazz.isAnnotationPresent(HttpProcessorConfig.class)) {

					Annotation annotation = clazz
							.getAnnotation(HttpProcessorConfig.class);
					HttpProcessorConfig mapper = (HttpProcessorConfig) annotation;

					System.out.println(mapper.uriPath());					
					System.out.println(mapper.contentType());

				}
			}
		}
		// classes.add(HttpNodeResourceHandler.class);
		return classes;
	}

	public static void main(String[] args) {
		// TestHttpProcessors testHttpProcessors = new TestHttpProcessors();
		System.out.println(getClasses());	
	}
}
