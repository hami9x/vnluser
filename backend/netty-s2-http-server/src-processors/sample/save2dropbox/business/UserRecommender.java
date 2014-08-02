package sample.save2dropbox.business;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;
import rfx.server.configs.NoSqlServerInfoConfigs;
import rfx.server.util.SocialAnalyticsUtil;
import rfx.server.util.StringUtil;
import rfx.server.util.Utils;
import sample.save2dropbox.model.Item;

import com.google.gson.Gson;

/**
 * @author trieu
 * 
 * the recommend items, the core idea is, modeling interest's user by using most used keywords from past to current 
 *
 */
public class UserRecommender {
	
	static String redisHost = NoSqlServerInfoConfigs.getServerInfo("REDIS_SERVER1").host;
	static int redisPort = NoSqlServerInfoConfigs.getServerInfo("REDIS_SERVER1").port;
	
	
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = (-1)*e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
	public static List<String> getTopKeywordsOfUser(int userId){
		String host = NoSqlServerInfoConfigs.getServerInfo("REDIS_SERVER1").host;
		int port = NoSqlServerInfoConfigs.getServerInfo("REDIS_SERVER1").port;
		Jedis jedis = new Jedis(host, port);
		
		Map<String, String> map = jedis.hgetAll("user:" + userId);
		jedis.close();
				
		ConcurrentHashMap<String, Integer> userKeywordStats = new ConcurrentHashMap<>();
		
		map.values().parallelStream().forEach((String json)->{
			try {
				//System.out.println(json);
				Item item = new Gson().fromJson(json, Item.class);				
				if(item.getKeywords() == null){
					return;
				}
				List<String> keywords = item.getKeywords();
				for (String keyword : keywords) {
					keyword = keyword.trim();
					if(!keyword.isEmpty()){
						int kf = userKeywordStats.getOrDefault(keyword, -1);
						if(kf < 0){
							kf = 1;
							userKeywordStats.put(keyword, kf);
						} else {
							kf++;
							userKeywordStats.put(keyword, kf);
						}
					}					
					
					//System.out.println(keyword+" -> " + kf);
				}				
			} catch (Exception e) {	}
		});
		//System.out.println(userKeywordStats);
//		userKeywordStats.put("a", 2);
//		userKeywordStats.put("Apple", 3);
//		userKeywordStats.put("IBM", 1);
//		userKeywordStats.put("Google", 23);
//		userKeywordStats.put("Facebook", 5);
//		userKeywordStats.put("Dell", 3);
		
		int top = 5;
		SortedSet<Entry<String, Integer>> sortedset = entriesSortedByValues(userKeywordStats);
		List<String> top5Keywords = new ArrayList<String>(top);
		
		for (Entry<String, Integer> entry : sortedset) {
		    //System.out.println(entry.getKey()+" => "+entry.getValue());
			top5Keywords.add(entry.getKey());
		    top--;
		    if(top <= 0){
		    	break;
		    }
		}
		return top5Keywords;
	}
	
	public static List<Item> recommendItems(int userId){
		List<String> dnaUser = getTopKeywordsOfUser(userId);
		return SearchEngineLucene.searchItemsByKeywords(dnaUser,userId);
	}
	
	public static void computeRecomendedItemsForUser(int userId){
		List<Item> items = SearchEngineLucene.searchItemsByKeywords(getTopKeywordsOfUser(userId),userId);
		Jedis jedis = new Jedis(redisHost, redisPort);
		items.stream().forEach((Item item)->{			
			System.out.println(item.getLink());
			int fbLike = SocialAnalyticsUtil.getFacebookLikeCount(item.getLink());	
			System.out.println(fbLike);
			String recKey = "user:" + item.getUser_id() + " post:" + item.getPost_id();
			jedis.zadd("recommend:"+userId, fbLike, recKey);//user:55455908 post:18
		});
		jedis.close();
	}
	
	public static void fullIndexingItems(){
		
		Jedis jedis = new Jedis(redisHost, redisPort);
		Set<String> userKeys = jedis.keys("user:*");
		userKeys.stream().forEach((String userkey)->{
			
			Map<String, String> map = jedis.hgetAll(userkey);
			System.out.println("indexing items of user " + userkey + " itemCount "+map.size());
			
			List<Item> userItems = new ArrayList<>();
			map.values().stream().forEach((String json)->{
				String k = "";
				int user_id = StringUtil.safeParseInt(userkey.replace("user:", ""));
				try {
					Item item = new Gson().fromJson(json, Item.class);
					item.setUser_id(user_id);
					//jedis.hget(KEY_INDEXED_ITEMS, item.)
					k = "p:"+item.getPost_id();
					System.out.println(" item " + k);
					String v = jedis.hget(KEY_INDEXED_ITEMS, k);
					if(v == null)
					{
						userItems.add(item);
						System.out.println("INDEXED "+k);
						jedis.hset(KEY_INDEXED_ITEMS, k,"1");
					}					
				} catch (Exception e) {
					System.err.println(e.getMessage() + " at k: " + k);
				}
			});
			SearchEngineLucene.indexItems(userItems);
		});
		jedis.close();
		Utils.sleep(500);		
		
		userKeys.stream().forEach((String userkey)->{
			int userId = StringUtil.safeParseInt(userkey.replace("user:", ""));
			computeRecomendedItemsForUser(userId );
		});
		Utils.sleep(500);
	}
	
	static final String KEY_INDEXED_ITEMS = "indexed-items";
	
	public static void scheduleJob(){
		Timer scheduledService = new Timer(true);//daemon process
		
		TimerTask autoTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				fullIndexingItems();
			}
		};
		long delay =  1;
		long period =  15;
		scheduledService.schedule(autoTask , delay *1000L, period*1000L);	
	}
	
	public static void main(String[] args) {
		//int userId = 47579516;
		//System.out.println(getTopKeywordsOfUser(userId));
//		List<Item> items = UserRecommender.recommendItems(userId);
//		for (Item item : items) {
//			System.out.println(item);
//		}
		
		scheduleJob();	
		while (true) {
			Utils.sleep(1000);			
		}
		
		//computeRecomendedItemsForUser(userId);
		//fullIndexingItems();
		
	}
	//issue: remove duplicated keywords: CSS and css is the same
	
}
