package rfx.server.test;

import java.io.IOException;

import rfx.server.http.data.service.DataService;
import rfx.server.http.data.service.ServerInfoService;
import rfx.server.util.template.DataServiceProcessingUtil;
import rfx.server.util.template.HandlebarsTemplateUtil;

public class TestProcessModel {

	public static void main(String[] args) throws IOException {
			
		
		DataServiceProcessingUtil.initTemplateConfigCache("rfx.server");
		
		DataService model = new ServerInfoService("all").build();
		String templateLocation = DataServiceProcessingUtil.getOutputConfig(model).template();
		String text = HandlebarsTemplateUtil.execute(templateLocation, model);
		System.out.println(templateLocation);
		
		
		String BASE_PACKAGE = "sample.http";
		DataServiceProcessingUtil.initTemplateConfigCache(BASE_PACKAGE );
		
	}
}
