package rfx.server.configs;

import java.util.HashMap;
import java.util.Map;

import rfx.server.util.LogUtil;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class LogFilterConfigs {

	static LogFilterConfigs _instance;

	Map<String, String> badIpMap = new HashMap<>();
	Map<String, String> refererDomains = new HashMap<>();
	Map<String, String> onlyWriteForDomains = new HashMap<>();
	Map<String, Boolean> writeLogsForTopics = new HashMap<>();
	
	String secretCodeForSecuredAPI;
	

	
	public String getSecretCodeForSecuredAPI() {
		if(secretCodeForSecuredAPI == null){
			secretCodeForSecuredAPI = "";
		}
		return secretCodeForSecuredAPI;
	}

	public void setSecretCodeForSecuredAPI(String secretCodeForSecuredAPI) {
		this.secretCodeForSecuredAPI = secretCodeForSecuredAPI;
	}

	
	public Map<String, String> getBadIpMap() {
		return badIpMap;
	}

	public void setBadIpMap(Map<String, String> ip) {
		this.badIpMap = ip;
	}

	public Map<String, String> getRefererDomains() {
		return refererDomains;
	}

	public void setRefererDomains(Map<String, String> refererDomains) {
		this.refererDomains = refererDomains;
	}
	
	
	
	public Map<String, String> getOnlyWriteForDomains() {
		return onlyWriteForDomains;
	}

	public void setOnlyWriteForDomains(Map<String, String> onlyWriteForDomains) {
		this.onlyWriteForDomains = onlyWriteForDomains;
	}
	

	public Map<String, Boolean> getWriteLogsForTopics() {
		return writeLogsForTopics;
	}

	public void setWriteLogsForTopics(Map<String, Boolean> writeLogsForTopics) {
		this.writeLogsForTopics = writeLogsForTopics;
	}

	public static LogFilterConfigs load(){
		if (_instance == null) {
			try {
				//String json = FileUtils.readFileAsString(StringPool.LOG_FILTERS_CONFIG_FILE);//FIXME
				_instance = new Gson().fromJson("", LogFilterConfigs.class);
				LogUtil.info("LogFilterConfigs loaded and create new instance from "+ ConfigManager.HTTP_FILTERS_CONFIG_FILE);
			} catch (Exception e) {
				if (e instanceof JsonSyntaxException) {
					e.printStackTrace();
					System.err.println("Wrong JSON syntax in file "+ConfigManager.HTTP_FILTERS_CONFIG_FILE);
				} else {
					e.printStackTrace();
				}
			}
		}
		return _instance;
	}

}
