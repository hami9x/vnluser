import redis.clients.jedis.Jedis;


Jedis jedis = new Jedis("127.0.0.1", 45384)

String[] infoLines = jedis.info().split("\n");

long maxLimitMemory = 1073741824;// 1 GB in bytes
infoLines.each {
	String line = (it.toString());
	if(line.contains("used_memory:")){
		line = line.replace("used_memory:", "")
		long bytes = Long.parseLong(line.trim());
		if(bytes > maxLimitMemory){
			println "OVER-LIMIT 1GB, used: "+bytes;
		} else {
			//skip
			println "OK used: "+bytes;
		}
	}
}
