package rfx.server.http.data;

import rfx.server.util.StringUtil;

public final class GeoLocation {
	public String geoCountry = "VN";
	public String geoCity = "THANH PHO HO CHI MINH";
	
	public GeoLocation(String geoCountry, String geoCity) {
		super();
		this.geoCountry = geoCountry;
		this.geoCity = geoCity;
	}
	public GeoLocation() {}
	
	@Override
	public String toString() {			
		return StringUtil.toString("geoCountry:",geoCountry," geoCity:",geoCity);
	}
}