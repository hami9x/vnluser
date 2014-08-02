package rfx.server.configs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.JedisPoolConfig;
import rfx.server.util.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RedisPoolConfigs implements Serializable {

    private static final long serialVersionUID = -6047539971043372940L;
    static RedisPoolConfigs _instance;
    
    Map<String, String> IPLocation;
    Map<String, String> RealtimePubSub;
    List<Map<String, String>> GenderRedisPools = new ArrayList<>();
    
    int cacheTime = 30;
    int maxActive = 20;
    int maxIdle = 10;
    int minIdle = 1;
    int maxWait = 3000;
    int numTestsPerEvictionRun = 10;
    boolean testOnBorrow = true;
    boolean testOnReturn = true;
    boolean testWhileIdle = true;
    int timeBetweenEvictionRunsMillis = 60000;
    int maxCacheEntry = 30;
    // provinceId cache when lookup location from IP range
    Map<Integer, Boolean> provinceCacheLookup; // {24:true,29:true}
    Map<String, Boolean> countryCacheLookup; // {VN:true,US:true}

    public Map<String, String> getIPLocation() {
        return IPLocation;
    }

    public void setIPLocation(Map<String, String> IPLocation) {
        this.IPLocation = IPLocation;
    }

    public Map<String, String> getRealtimePubSub() {
		return RealtimePubSub;
	}

	public void setRealtimePubSub(Map<String, String> realtimePubSub) {
		RealtimePubSub = realtimePubSub;
	}

	public List<Map<String, String>> getGenderRedisPools() {
		return GenderRedisPools;
	}

	public void setGenderRedisPools(List<Map<String, String>> genderRedisPools) {
		GenderRedisPools = genderRedisPools;
	}

	public static final RedisPoolConfigs load() {
        if (_instance == null) {
            try {
                String json = FileUtils.readFileAsString(ConfigManager.REDIS_CONFIG_FILE);
                _instance = new Gson().fromJson(json, RedisPoolConfigs.class);
            } catch (Exception e) {
                if (e instanceof JsonSyntaxException) {
                    e.printStackTrace();
                    System.err.println("Wrong JSON syntax in file : "+ConfigManager.REDIS_CONFIG_FILE);
                } else {
                    e.printStackTrace();
                }
            }
        }
        return _instance;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public int getCacheTime() {
        return this.cacheTime;
    }

    public void setCacheTime(int cacheTime) {
        this.cacheTime = cacheTime;
    }

    public int getMaxCacheEntry() {
        return maxCacheEntry;
    }

    public void setMaxCacheEntry(int maxCacheEntry) {
        this.maxCacheEntry = maxCacheEntry;
    }
    
	public Map<Integer, Boolean> getProvinceCacheLookup() {
		return provinceCacheLookup;
	}

	public void setProvinceCacheLookup(Map<Integer, Boolean> provinceCacheLookup) {
		this.provinceCacheLookup = provinceCacheLookup;
	}
	
	public Map<String, Boolean> getCountryCacheLookup() {
		return countryCacheLookup;
	}

	public void setCountryCacheLookup(Map<String, Boolean> countryCacheLookup) {
		this.countryCacheLookup = countryCacheLookup;
	}

	public JedisPoolConfig getJedisPoolConfig() {       
        return RedisConnectionPoolConfig.getJedisPoolConfigInstance();
    }
}
