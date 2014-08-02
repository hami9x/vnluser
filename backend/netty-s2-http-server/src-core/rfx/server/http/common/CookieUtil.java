package rfx.server.http.common;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.USER_AGENT;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.ServerCookieEncoder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import rfx.server.http.cookie.CookieData;
import rfx.server.util.LocationUtil;
import rfx.server.util.SecurityUtil;

public class CookieUtil {
	public final static long COOKIE_AGE_10_YEARS = 630720000;
	public final static long COOKIE_AGE_2_YEARS = 63072000;
	public final static long COOKIE_AGE_1_YEAR = 31536000;
	public final static long COOKIE_AGE_1_HOUR = 3600; // One hour
	public final static long COOKIE_AGE_2_HOURS = 7200; // 2 hours
	public final static long COOKIE_AGE_3_HOURS = 10800; // 3 hours	
	public final static long COOKIE_AGE_1_DAY = 86400; // One day
	public final static long COOKIE_AGE_3_DAYS = 259200; // 3 days
	public final static long COOKIE_AGE_1_WEEK = 604800; // One week

	/*
	 * rand format 3b10f37d26bae61d.1330937373.4.1331004249.1330998456.2
	 * userid. first time visit, number visit, current time, last time visit, locationId
	 */
	public static String newSessionValue(String rand_aid, int rand_locationId) {
		int rand_numbervisit = 1;
		int rand_numberidvisit = 1;
		long unixTime = System.currentTimeMillis() / 1000L;
		int rand_currenttime = (int) unixTime;
		int rand_firsttime = rand_currenttime;
		int rand_lasttimevisit = rand_currenttime;
		return CookieUtil.generateSessionString(rand_aid, rand_firsttime,
				rand_numbervisit, rand_currenttime, rand_lasttimevisit,rand_numberidvisit,
				rand_locationId);
	}

	public static String generateSessionString(String rand_aid, int rand_firsttime,int rand_numbervisit, int rand_currenttime, 
			int rand_lasttimevisit,int rand_numberidvisit,	int rand_locationId) {
		StringBuilder cookieString = new StringBuilder();
		cookieString.append(rand_aid);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_firsttime);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_numbervisit);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_currenttime);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_lasttimevisit);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_numberidvisit);
		cookieString.append(CookieData.COOKIE_SEPARATOR);
		cookieString.append(rand_locationId);		
		return SecurityUtil.encryptBeaconValue(cookieString.toString());
	}

	public static void setAnomyousCookie(Cookie cookie,	FullHttpResponse response) {
		response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
	}

	public static Cookie getAnomyousCookie(HttpRequest request) {
		Cookie fospAid = null;
		String cookieString = request.headers().get(COOKIE);

		if (cookieString != null) {
			try {
				cookieString = URLDecoder.decode(cookieString, "UTF-8");
			} catch (UnsupportedEncodingException e) {			
				e.printStackTrace();
			}
			Set<Cookie> cookies = CookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					String name = cookie.getName();
					// String value = cookie.getValue();
					// TODO: Check validate PK_UID
					if (name.equals(CookieData.USER_ID)) {
						fospAid = cookie;
						addExpireTime2YearsForCookie(fospAid);
						fospAid.setDomain(CookieData.DEFAULT_DOMAIN);
						fospAid.setPath(CookieData.DEFAULT_PATH);
						//hasFospId = true;
						// case PK_UID:
						// hasPkUid = true;
						// break;
					}
				}
			}
		}	
		return fospAid;
	}

	public static boolean validateTrueCookie(HttpRequest request) {
		return true;
	}

	public static boolean isValidFospAid(String fosp_aid) {
		if (fosp_aid.length() == 16 && fosp_aid.matches("^[0-9A-Fa-f]+$")) {
			return true;
		} else {
			return false;
		}
	}

	public static Cookie createNewHttpOnlyCookie(String name, String value,
			String domain, String path) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setDomain(domain);
		cookie.setPath(path);
		cookie.setHttpOnly(true);
		return cookie;
	}

	public static Cookie createCookie(String name, String value, String domain,
			String path) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setDomain(domain);
		cookie.setPath(path);
		return cookie;
	}

	public static String generateUserIdCookieString(HttpRequest request) {		
		String userAgent = request.headers().get(USER_AGENT);
		String logDetails = request.headers().get(io.netty.handler.codec.http.HttpHeaders.Names.HOST);
		String result = SecurityUtil.sha1(userAgent + logDetails + System.currentTimeMillis());
		return result.substring(0, 16);
	}
	
	// create ID for browser not support cookies and localStorage
	public static String generateUserIdByIp(String ipAddress, String browser, String os) {		
		String result = SecurityUtil.sha1(ipAddress + browser + os);
		return result.substring(0, 16);
	}

	static void setDefaultCookieInfo(Cookie cookie) {
		cookie.setMaxAge(COOKIE_AGE_2_YEARS);
		cookie.setPath(CookieData.DEFAULT_DOMAIN);
		cookie.setPath(CookieData.DEFAULT_PATH);
		cookie.setHttpOnly(true);
	}

	public static void addExpireTime2YearsForCookie(Cookie cookie) {
		cookie.setMaxAge(COOKIE_AGE_2_YEARS);
	}
	
	public static void addExpireTimeForCookie(Cookie cookie, long maxAge) {
		cookie.setMaxAge(maxAge);
	}
	

	public static int getIntParam(String[] params, int position) {
		int result = LocationUtil.LOCATION_NULL;
		if (params.length > position) {
			try {
				result = Integer.parseInt(params[position]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String getStrParam(String[] params, int position) {
		String result = "";
		try {
			result = params[position];
		} catch (Exception e) {
			result = "";
		}
		return result;
	}

	public static FullHttpResponse handleGetIdPath(HttpRequest request,String ipAdress, String uri)  {		
		// http://example.com/getid?callback=callback
		return new CookieData(request, ipAdress, uri).responseForGetId();
	}
	
	public static Cookie createCookie(String name, String value, String domain, String path, long maxAge) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setDomain(domain);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		return cookie;
	}
	
	public static Cookie createCookie(String name, String value, long maxAge) {
		Cookie cookie = new DefaultCookie(name, value);		
		cookie.setMaxAge(maxAge);		
		return cookie;
	}
	
	public static void setCookie(String name, String value, String domain, String path, long maxAge, FullHttpResponse response) {
		Cookie cookie = createCookie(name, value, domain, path, maxAge);
		response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
	}

}
