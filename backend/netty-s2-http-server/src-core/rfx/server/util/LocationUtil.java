package rfx.server.util;

/* Lookup geographic location from IP address
 * Author: TrucLK
 * Idea: http://stackoverflow.com/questions/8622816/redis-or-mongo-for-determining-if-a-number-falls-within-ranges/8624231#8624231
 * Sơ lược ý tưởng:
 * Mỗi IP address có thể được đổi thành một số nguyên hệ thập phân.
 *
 * Một dãy IP bao gồm 1 số đầu và số cuối, thể hiện trên dãy đó là mã số của vị trí tỉnh thành.
 *
 * Cách thức lưu trữ dữ liệu gồm 1 Sorted set của Redis. Mỗi dãy IP sẽ có 2 vị trí trên set này. Với điểm số tương đương với giá trị đầu và giá trị cuối của dãy.
 *
 * Tìm 2 giá trị gần nhất và xem có cùng thuộc 1 dãy hay không. Nếu thuộc sẽ lấy giá trị Location của dãy ấy.
 *
 * Khuyết điểm, vẫn chỉ mới hỗ trợ các dãy IP riêng không chồng chéo.
 *
 * */
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import rfx.server.util.redis.RedisManagerUtil;

public class LocationUtil {

    final public static String MAXIP = "4294967295";
    final public static int LOCATION_VIETNAM_UNDEFINED = 0;
    final public static int LOCATION_UNDEFINED = -99;
    final public static int LOCATION_NULL = -1;
    private static Map<Integer, Boolean> provinceCacheMap = RedisManagerUtil.getRedisPoolConfigs().getProvinceCacheLookup() ; // {24:true,29:true}
    private static Map<String, Boolean> countryCacheMap = RedisManagerUtil.getRedisPoolConfigs().getCountryCacheLookup() ; // {VN:true,US:true}

    private static NavigableMap<Long, LocationCacheObj> LOCATIONCACHE = new TreeMap<Long, LocationCacheObj>();
    private static NavigableMap<Long, LocationCacheObj> COUNTRYCACHE = new TreeMap<Long, LocationCacheObj>();
    static int cachedTime = RedisManagerUtil.getRedisPoolConfigs().getCacheTime();

    /**
     * Lookup province ip, có cache các dãy IP của HCM, HA NOI (request lớn) ---> improve performance đáng kể
     * @param ipAdress
     * @return
     *   provinceId
     */
    public static LocationCacheObj getVNProvinceFromIp(String ipAdress) {

    	LocationCacheObj locationCacheObj = null;
        long ipLong = 0;
        try {
        	ipLong = StringUtil.Dot2LongIP(ipAdress);
            LocationCacheObj floorCacheObj 	  = LOCATIONCACHE.get(LOCATIONCACHE.floorKey(ipLong)); // chận dưới lớn nhất
            LocationCacheObj ceilCacheObj     = LOCATIONCACHE.get(LOCATIONCACHE.ceilingKey(ipLong)); // chận trên nhỏ nhất
            if (floorCacheObj.getProvince() != LOCATION_UNDEFINED) {
            	if( floorCacheObj.beginIPNumber == ceilCacheObj.beginIPNumber
            		&& floorCacheObj.endIPNumber == ceilCacheObj.endIPNumber ){
            		//System.out.println( "return from cache, begin: "+ floorCacheObj.getBeginIPNumber() + ","+floorCacheObj.getEndIPNumber() );
            		return floorCacheObj;
            	}
            }
        } catch (Exception ex) {}

        boolean commited = false;
        ShardedJedisPool jedisPool = null;
        ShardedJedis shardedJedis = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisManagerUtil.getIPLocationPool();
            shardedJedis = jedisPool.getResource();
            jedis = shardedJedis.getShard(StringPool.BLANK);
            Set<String> floors = jedis.zrevrangeByScore("range_index", String.valueOf(ipLong), "0", 0, 1);
            Set<String> ceils = jedis.zrangeByScore("range_index", String.valueOf(ipLong), MAXIP, 0, 1);
            if (floors.size() == 1 && ceils.size() == 1) {
                String floor = floors.iterator().next();
                String ceil = ceils.iterator().next();
                String f0, f1, c0, c1 = "";
                f0 = floor.split("-")[0];
                f1 = floor.split("-")[1];
                c0 = ceil.split("-")[0];
                c1 = ceil.split("-")[1];

                // 1: (f0.equals(c1) && f1.equals(c0))==true : Khi startIpNum < ipLong < endIpNum
                // 2: (f0.equals(c0) && f1.equals(c1))==true : startIpNum == ipLong AND ipLong == endIpNum
                // 2.1 Nếu startIpNum == ipLong, floor = ceil = startIpNum-endIpNum
                // 2.2 Nếu endIpNum   == ipLong, floor = ceil = endIpNum-startIpNum --> phải đảo lại startIpNum-endIpNum để lấy province,zone
                if ( (f0.equals(c1) && f1.equals(c0))  || (f0.equals(c0) && f1.equals(c1)) ) {

                	int zone = 0 ;
                	int province =  StringUtil.safeParseInt(jedis.hget("province:" + floor,"province"), LOCATION_UNDEFINED);
                    if( province == LOCATION_UNDEFINED ){
                    	System.out.println( "2.2 endIpNum == ipLong:"+f1+"-"+f0 );
                    	province =  StringUtil.safeParseInt(jedis.hget("province:"+f1+"-"+f0,"province"), LOCATION_UNDEFINED);
                    	zone =  StringUtil.safeParseInt(jedis.hget("province:"+f1+"-"+f0,"zone"), 0);
                    }
                    else{
                    	zone =  StringUtil.safeParseInt(jedis.hget("province:" + floor,"zone"), 0);
                    }

                	long beginIPNumber = Long.parseLong(f0) ;
                	long endIPNumber   = Long.parseLong(f1) ;

                	locationCacheObj = new LocationCacheObj(cachedTime);
                	locationCacheObj.setProvice(province);
                	locationCacheObj.setZone(zone);
                	locationCacheObj.setBeginIPNumber(beginIPNumber);
                	locationCacheObj.setEndIPNumber(endIPNumber);

                    LocationCacheObj ceilCacheObj = new LocationCacheObj();
                    ceilCacheObj.setProvice(province);
                    ceilCacheObj.setZone(zone);
                    ceilCacheObj.setBeginIPNumber(beginIPNumber);
                    ceilCacheObj.setEndIPNumber(endIPNumber);

                    if (LOCATIONCACHE.size() < RedisManagerUtil.getRedisPoolConfigs().getMaxCacheEntry() ) {
                    	if( provinceCacheMap.get(province) != null ){
	                    	LOCATIONCACHE.put(beginIPNumber, locationCacheObj);
	                		LOCATIONCACHE.put(endIPNumber, ceilCacheObj);
                		}
                	}
                }
            }
            commited = true;
            // zrevrangebyscore range_index 5 0 LIMIT 0 1
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("LocationUtil.getLocationFromIp", e.getMessage());
        } finally {
            if (commited) {
                jedisPool.returnResource(shardedJedis);
            } else {
                jedisPool.returnBrokenResource(shardedJedis);
            }
        }
        if(locationCacheObj == null) {
        	locationCacheObj = new LocationCacheObj();
        }
        return locationCacheObj;
    }

    /**
     * Lookup country ip, có cache các dãy IP của VN, US (request lớn) ---> improve performance đáng kể
     * @param ipAdress
     * @return
     *   countryCode
     */
    public static LocationCacheObj getCountryFromIp(String ipAdress) {

    	LocationCacheObj locationCacheObj = null;
        long ipLong = 0;
        try {
        	ipLong = StringUtil.Dot2LongIP(ipAdress);
            LocationCacheObj floorCacheObj 	  = COUNTRYCACHE.get(COUNTRYCACHE.floorKey(ipLong)); // chận dưới lớn nhất
            LocationCacheObj ceilCacheObj     = COUNTRYCACHE.get(COUNTRYCACHE.ceilingKey(ipLong)); // chận trên nhỏ nhất
            if (floorCacheObj.getCountryCode() != null) {
            	if( floorCacheObj.beginIPNumber == ceilCacheObj.beginIPNumber
            		&& floorCacheObj.endIPNumber == ceilCacheObj.endIPNumber ){
            		//System.out.println( "getCountryFromIp return from cache, begin: "+ floorCacheObj.getBeginIPNumber() + ","+floorCacheObj.getEndIPNumber() + ", country: "+ floorCacheObj.getCountryCode() );
            		return floorCacheObj;
            	}
            }
        } catch (Exception ex) {}

        boolean commited = false;
        ShardedJedisPool jedisPool = null;
        ShardedJedis shardedJedis = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisManagerUtil.getIPLocationPool();
            shardedJedis = jedisPool.getResource();
            jedis = shardedJedis.getShard(StringPool.BLANK);
            Set<String> floors = jedis.zrevrangeByScore("c_range_index", String.valueOf(ipLong), "0", 0, 1);
            Set<String> ceils = jedis.zrangeByScore("c_range_index", String.valueOf(ipLong), MAXIP, 0, 1);
            if (floors.size() == 1 && ceils.size() == 1) {
                String floor = floors.iterator().next();
                String ceil = ceils.iterator().next();

                String f0, f1, c0, c1 = "";
                f0 = floor.split("-")[0];
                f1 = floor.split("-")[1];
                c0 = ceil.split("-")[0];
                c1 = ceil.split("-")[1];

                // 1: (f0.equals(c1) && f1.equals(c0))==true : Khi startIpNum < ipLong < endIpNum
                // 2: (f0.equals(c0) && f1.equals(c1))==true : startIpNum == ipLong AND ipLong == endIpNum
                // 2.1 Nếu startIpNum == ipLong, floor = ceil = startIpNum-endIpNum
                // 2.2 Nếu endIpNum   == ipLong, floor = ceil = endIpNum-startIpNum --> phải đảo lại startIpNum-endIpNum để lấy countryCode
                if ( (f0.equals(c1) && f1.equals(c0)) || (f0.equals(c0) && f1.equals(c1)) ) {

                	String countryCode =  jedis.hget("country:"+floor,"code");
                    if( countryCode==null ){
                    	countryCode =  jedis.hget("country:"+f1+"-"+f0,"code");
                    }

                	long beginIPNumber = Long.parseLong(f0) ;
                	long endIPNumber   = Long.parseLong(f1) ;

                	locationCacheObj = new LocationCacheObj(cachedTime);
                	locationCacheObj.setCountryCode(countryCode);
                	locationCacheObj.setBeginIPNumber(beginIPNumber);
                	locationCacheObj.setEndIPNumber(endIPNumber);

                    LocationCacheObj ceilCacheObj = new LocationCacheObj();
                    locationCacheObj.setCountryCode(countryCode);
                    ceilCacheObj.setBeginIPNumber(beginIPNumber);
                    ceilCacheObj.setEndIPNumber(endIPNumber);

                    if (COUNTRYCACHE.size() < RedisManagerUtil.getRedisPoolConfigs().getMaxCacheEntry() ) {
                    	if( countryCacheMap.get(countryCode) != null ){
                    		//System.out.println( "COUNTRYCACHE.put "+ beginIPNumber);
                    		COUNTRYCACHE.put(beginIPNumber, locationCacheObj);
                    		COUNTRYCACHE.put(endIPNumber, ceilCacheObj);
                		}
                	}
                }
            }
            commited = true;
            // zrevrangebyscore range_index 5 0 LIMIT 0 1
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("LocationUtil.getLocationFromIp", e.getMessage());
        } finally {
            if (commited) {
                jedisPool.returnResource(shardedJedis);
            } else {
                jedisPool.returnBrokenResource(shardedJedis);
            }
        }
        if(locationCacheObj == null) {
        	locationCacheObj = new LocationCacheObj();
        }
        return locationCacheObj;
    }

    @Deprecated
    /**
     * Not cache version
     *
     * @param ipAdress
     * @return
     */
    public static LocationCacheObj getLocationFromIpNotCache(String ipAdress) {

    	LocationCacheObj locationCacheObj = null;
        long ipLong = 0;
        try {
        	ipLong = StringUtil.Dot2LongIP(ipAdress);
        } catch (Exception ex) {}

        boolean commited = false;
        ShardedJedisPool jedisPool = null;
        ShardedJedis shardedJedis = null;
        Jedis jedis = null;
        try {
            jedisPool = RedisManagerUtil.getIPLocationPool();
            shardedJedis = jedisPool.getResource();
            jedis = shardedJedis.getShard(StringPool.BLANK);
            Set<String> floors = jedis.zrevrangeByScore("range_index", String.valueOf(ipLong), "0", 0, 1);
            Set<String> ceils = jedis.zrangeByScore("range_index", String.valueOf(ipLong), MAXIP, 0, 1);
            if (floors.size() == 1 && ceils.size() == 1) {
                String floor = floors.iterator().next();
                String ceil = ceils.iterator().next();
                String f0, f1, c0, c1 = "";
                f0 = floor.split("-")[0];
                f1 = floor.split("-")[1];
                c0 = ceil.split("-")[0];
                c1 = ceil.split("-")[1];
                if (f0.equals(c1) && f1.equals(c0)) {
                    int province =  StringUtil.safeParseInt(jedis.hget("province:" + floor,"province"), LOCATION_UNDEFINED);
                    int zone =  StringUtil.safeParseInt(jedis.hget("province:" + floor,"zone"), 1);
                    if (LOCATIONCACHE.size() < RedisManagerUtil.getRedisPoolConfigs().getMaxCacheEntry()) {
                    	locationCacheObj = new LocationCacheObj(cachedTime);
                    	locationCacheObj.setProvice(province);
                    	locationCacheObj.setZone(zone);
                    }
                }
            }
            commited = true;
            // zrevrangebyscore range_index 5 0 LIMIT 0 1
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("LocationUtil.getLocationFromIp", e.getMessage());
        } finally {
            if (commited) {
                jedisPool.returnResource(shardedJedis);
            } else {
                jedisPool.returnBrokenResource(shardedJedis);
            }
        }
        if(locationCacheObj == null) {
        	locationCacheObj = new LocationCacheObj();
        }
        return locationCacheObj;
    }

    public static class LocationCacheObj {

    	private long beginIPNumber = 0;
    	private long endIPNumber   = 0;

    	private String countryCode;
        private int province = LOCATION_UNDEFINED;
        private int zone = 0;
        private long cacheDay = new Date().getTime();
        private int liveTime = 30; // minus

        public LocationCacheObj() {
        }

        public LocationCacheObj(int liveTime) {
            this.liveTime = liveTime;
        }

        public int getProvince() {
            return province;
        }

        public void setProvice(int province) {
            this.province = province;
        }

        public int getZone() {
			return zone;
		}

		public void setZone(int zone) {
			this.zone = zone;
		}

		public boolean isExpried() {
            Date currentDate = new Date();
            return ((currentDate.getTime() - cacheDay) / (60 * 1000)) > liveTime;
        }

        public void setLiveTime(int mins) {
            this.liveTime = mins;
        }

		public long getBeginIPNumber() {
			return beginIPNumber;
		}

		public void setBeginIPNumber(long beginIPNumber) {
			this.beginIPNumber = beginIPNumber;
		}

		public long getEndIPNumber() {
			return endIPNumber;
		}

		public void setEndIPNumber(long endIPNumber) {
			this.endIPNumber = endIPNumber;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

    }

}
