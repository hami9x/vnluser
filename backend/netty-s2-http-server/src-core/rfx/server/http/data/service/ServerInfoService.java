package rfx.server.http.data.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import rfx.server.http.data.DataServiceConfig;
import rfx.server.monitor.util.MemoryWatcher;

@DataServiceConfig(template = "system/server-info")
public class ServerInfoService extends WebDataService {
	static final String classpath = ServerInfoService.class.getName();
	String time;
	List<Info> infos = new ArrayList<>();
	String filter;
	boolean showAll;
	boolean showCompact;
	String memoryStats;
	
	String rfxServerVersion = "1.0";
	
	static class Info {
		String data;
		int order;
		
		public Info(String data, int order) {
			setData(data);
			setOrder(order);
		}
		public void setData(String data) {
			this.data = data;
		}
		public String getData() {
			return data;
		}
		
		public int getOrder() {
			return order;
		}
		public void setOrder(int order) {
			this.order = order;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return data;
		}
	}

	public ServerInfoService(String filter) {
		this.filter = filter;		
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public List<Info> getInfos() {
		return infos;
	}

	public void setInfos(List<Info> infos) {
		this.infos = infos;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public boolean isShowCompact() {
		return showCompact;
	}

	public void setShowCompact(boolean showCompact) {
		this.showCompact = showCompact;
	}

	public String getMemoryStats() {
		return memoryStats;
	}

	public void setMemoryStats(String memoryStats) {
		this.memoryStats = memoryStats;
	}

	public String getRfxServerVersion() {
		return rfxServerVersion;
	}

	public void setRfxServerVersion(String rfxServerVersion) {
		this.rfxServerVersion = rfxServerVersion;
	}

	@Override
	public void freeResource() {
		infos.clear();
	}

	@Override
	public WebDataService build() {
		time = new Date().toString();
		if (filter.equals("all")) {
			showAll = true;
			showCompact = false;
			memoryStats = MemoryWatcher.collectMemoryStats();
		} else if (filter.equals("compact")) {
			showAll = false;
			showCompact = true;
		}

		Properties props = System.getProperties();
		Enumeration<?> e = props.propertyNames();
		int i = 0;
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			String s = (key + " : " + props.getProperty(key));			
			if (showAll) {
				i++;
				infos.add(new Info(s,i));
			} else if (showCompact) {
				{
					if (key.startsWith("java.vm")) {
						i++;
						infos.add(new Info(s,i));
					}
				}
			}

		}
		//
		return this;
	}

	@Override
	public String getClasspath() {
		return classpath;
	}
}
