package rfx.server.test;

import java.io.IOException;

import rfx.server.util.memcache.MemcacheCommand;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TestUtils {
	static void testMemcache() {
		try {
			// init memcache
			final String key = "w1372135767";
			String poolname = "WEBSITE_DETAIL_ADN_MC";

			String json = (new MemcacheCommand<String>(poolname) {
				@Override
				protected String build() {
					return mcClient.get(key).toString();
				}
			}).execute();

			System.out.println(json);
			JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
			System.out.println(jsonObject.get("url").getAsString());
			//MemcacheUtil.freeMemcachedResource(poolname);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testMemcache();
	}
}
