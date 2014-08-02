package rfx.server.monitor.model;

import java.util.List;
import java.util.Map;

public class ComparableMetricData {
	private String name;
	private Map<String, List<Long>> time;
	private long valuePeriod1;
	private long valuePeriod2;
	// (valuePeriod1 - valuePeriod2)/valuePeriod2 % 100
	private double variation;
	private Map<Long, Long> metric1;
	private Map<Long, Long> metric2;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<Long>> getTime() {
		return time;
	}

	public void setTime(Map<String, List<Long>> time) {
		this.time = time;
	}

	public long getValuePeriod1() {
		return valuePeriod1;
	}

	public void setValuePeriod1(long valuePeriod1) {
		this.valuePeriod1 = valuePeriod1;
	}

	public long getValuePeriod2() {
		return valuePeriod2;
	}

	public void setValuePeriod2(long valuePeriod2) {
		this.valuePeriod2 = valuePeriod2;
	}

	public double getVariation() {
		return variation;
	}

	public void setVariation(double variation) {
		this.variation = variation;
	}

	public Map<Long, Long> getMetric1() {
		return metric1;
	}

	public void setMetric1(Map<Long, Long> metric1) {
		this.metric1 = metric1;
	}

	public Map<Long, Long> getMetric2() {
		return metric2;
	}

	public void setMetric2(Map<Long, Long> metric2) {
		this.metric2 = metric2;
	}

}
