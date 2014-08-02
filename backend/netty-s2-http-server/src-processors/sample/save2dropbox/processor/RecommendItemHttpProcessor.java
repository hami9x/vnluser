package sample.save2dropbox.processor;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.util.StringUtil;
import sample.save2dropbox.model.Item;

import com.google.gson.annotations.Expose;

/**
 * @author trieu
 *
 */
@HttpProcessorConfig(uriPath= "/item-recommend", contentType = ContentTypePool.JSON)
public class RecommendItemHttpProcessor extends HttpProcessor {
	
	@Override
	protected DataService process(HttpRequestEvent e) {
		System.out.println("UserTrackingProcessor "+e);		
		try {
			String kind = e.param("kind","item");
			int userId = StringUtil.safeParseInt(e.param("userId"));
			if("item".equals(kind)){
				return new ItemDataService().build(userId);
			}
		} catch (Exception e1) {}
		return EMPTY;
	}
	
	static class ItemDataService implements DataService{
		static final String classpath = ItemDataService.class.getName();
		@Expose
		List<Item> items;
		
		@Expose
		String context = "Items you may like";

		@Override
		public void freeResource() {
			items.clear();
		}

		@Override
		public String getClasspath() {
			return classpath;
		}

		@Override
		public boolean isRenderedByTemplate() {
			return false;
		}

		@Override
		public List<HttpHeaders> getHttpHeaders() {
			// TODO Auto-generated method stub
			return null;
		}			
		
		public List<Item> getItems() {
			return items;
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}

		public ItemDataService build(int userId){
			 //items = SearchEngineLucene.searchItemsByKeywords(Arrays.asList("safari","chrome"), userId);
			 //items = UserRecommender.recomendItems(userId);
			 return this;
		}
		
	}

}
