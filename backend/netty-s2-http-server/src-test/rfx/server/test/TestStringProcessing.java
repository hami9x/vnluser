package rfx.server.test;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

public class TestStringProcessing {

	public final static  String[] REFERER_SEARCH_LIST = new String[]{"\t%s","\t","%s","\r\n","\n","\r"};
	public final static  String[] REFERER_REPLACE_LIST = new String[]{"","","","","",""};
	
	static class User {
		String name;
		int age;
		public User(String name, int age) {
			super();
			this.name = name;
			this.age = age;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		String refererUrl = "ab\tc\ndef";
		System.out.println(refererUrl);
		refererUrl = StringUtils.replaceEach(refererUrl, REFERER_SEARCH_LIST,  REFERER_REPLACE_LIST);
		System.out.println(refererUrl);
		
		String u = "aHR0cDovL2dhY3NhY2guY29tL3RodS12aWVuLXNhY2g@cGFnZT00".replace("@", "/");		
		System.out.println("urf: "+ new String(Base64.getDecoder().decode(u)));
		
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "value 1");
		model.put("blogs", Arrays.asList("title1","title2"));
		model.put("users", Arrays.asList(new User("trieu", 28), new User("Khoa", 1)));
		
		TemplateLoader loader = new FileTemplateLoader("C:\\Users\\trieu.nguyen\\git\\netty-s2-http-server\\resources\\tpl\\handlebars", ".html");
		Handlebars handlebars = new Handlebars(loader);
		Template template = handlebars.compile("test");
		System.out.println(template.apply(model));
	}
}
