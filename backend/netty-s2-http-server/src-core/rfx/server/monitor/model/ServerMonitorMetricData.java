package rfx.server.monitor.model;

import java.util.Map;

public class ServerMonitorMetricData {
	
	String currentFormatedTime;
	long currentTotalRequest;

	private Map<Integer, Integer> requestPerMinute;
	private Map<Integer, Integer> requestPerSecond;
	public String getCurrentFormatedTime() {
		return currentFormatedTime;
	}
	public void setCurrentFormatedTime(String currentFormatedTime) {
		this.currentFormatedTime = currentFormatedTime;
	}
	public long getCurrentTotalRequest() {
		return currentTotalRequest;
	}
	public void setCurrentTotalRequest(long currentTotalRequest) {
		this.currentTotalRequest = currentTotalRequest;
	}
	public Map<Integer, Integer> getRequestPerMinute() {
		return requestPerMinute;
	}
	public void setRequestPerMinute(Map<Integer, Integer> requestPerMinute) {
		this.requestPerMinute = requestPerMinute;
	}
	public Map<Integer, Integer> getRequestPerSecond() {
		return requestPerSecond;
	}
	public void setRequestPerSecond(Map<Integer, Integer> requestPerSecond) {
		this.requestPerSecond = requestPerSecond;
	}


}
