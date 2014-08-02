package rfx.server.http.cookie;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rfx.server.configs.HttpServerConfigs;
import rfx.server.http.common.CookieUtil;
import rfx.server.http.common.NettyHttpUtil;
import rfx.server.log.handlers.StaticFileHandler;
import rfx.server.util.SecurityUtil;
import rfx.server.util.StringPool;
import rfx.server.util.StringUtil;

import com.google.gson.Gson;

public class CookieData {
	
	public final static String P3P = "CP='CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE IND PHY ONL COM NAV OTC NOI DSP COR IDC'";
	public final static String USER_ID = "userid";
	public final static String PK_UID = "_pk_uid";
	public final static String DEFAULT_DOMAIN = HttpServerConfigs.load().getCookieDomain();
	public final static String DEFAULT_PATH = "/";
	public final static String COOKIE_SEPARATOR = ".";

	public final static String USER_ID_COOKIE_NAME = "userid";

	public final static String SESSION_COOKIE_NAME = "usersession";
	public final static String TRACKING_VERSION = "eatv";
	public final static String EHASHED = "ehashed";
	
	public final static String PK_UID_NAME = "_pk_uid";
	public static final String fosp_sessionKW ="fosp_session%22%2C%22";
	
	public final static long TIME_FOR_EACH_VISIT = 1800; // 30 minus
	
	// Get All Cookie
	String userid = "";
	String userSession = "";

	
	String ehashed = "";
	String rand_aid = "";
	int rand_firsttime = 0;
	int rand_numbervisit = 0;
	// number visit in for one rand_aid. If a visitor comes to your website for the first time, 
	// or if he visits a page (or downloads a file) more than 30 minutes after his last page view, this will be recorded as a new visit
	int rand_numberidvisit = 0;   
	int rand_currenttime = 0;
	int rand_lasttimevisit = 0;
	int rand_locationId = 9999;
	
	boolean is_new_location_ID = true;
	
	HttpRequest request;
	String ipAdress;
	String uri;
	Cookie fospAidCookie;
	
	
	public CookieData(HttpRequest request,String ipAdress, String uri)  {	
		this.request = request;
		this.ipAdress = ipAdress;
		this.uri = uri;
	}
	
	void updateFospSession(){	
		String[] params = userSession.split("\\.");
		rand_aid = CookieUtil.getStrParam(params, 0);
		userid = rand_aid;
		rand_firsttime 		= CookieUtil.getIntParam(params, 1);
		rand_numbervisit 	= CookieUtil.getIntParam(params, 2);
		rand_currenttime 	= CookieUtil.getIntParam(params, 3);
		rand_lasttimevisit  = CookieUtil.getIntParam(params, 4);
		rand_numberidvisit  = CookieUtil.getIntParam(params, 5);
		int rand_locationId = CookieUtil.getIntParam(params, 6);
		
		
		if( rand_numberidvisit==-1 ){ // first time access
			rand_numberidvisit = 1;
		}
		else{  
			if( System.currentTimeMillis()/1000L - rand_currenttime > TIME_FOR_EACH_VISIT ){ // > 30 minus
				rand_numberidvisit +=1;
			}
		}

		rand_numbervisit   = rand_numbervisit + 1;
		rand_lasttimevisit = rand_currenttime;
		rand_currenttime   = (int) (System.currentTimeMillis() / 1000L);
		
		userSession = CookieUtil.generateSessionString(rand_aid,	rand_firsttime, rand_numbervisit, rand_currenttime,	rand_lasttimevisit,rand_numberidvisit,rand_locationId);
	}
	
	protected void setCookieDataFromRequest(){
		try {
			String cookieString = request.headers().get(COOKIE);
			if (cookieString != null) {
				cookieString = URLDecoder.decode(cookieString, "UTF-8");
				Set<Cookie> cookies = CookieDecoder.decode(cookieString);				
				
				for (Cookie cookie : cookies) {
					String name = cookie.getName();
					String value = cookie.getValue();
					if (USER_ID_COOKIE_NAME.equals(name)) {
						if(value != null){
							userid = value;
							this.fospAidCookie = cookie;
						}
					} else if (SESSION_COOKIE_NAME.equals(name)) {
						if(value != null){
							userSession = value;					
						}
					} else if(EHASHED.equals(name)){
						this.ehashed = value;
					}
				}				
			}
			
			if (cookieString == null) {
				int fosp_sessionIx = request.getUri().indexOf(fosp_sessionKW);
				if( fosp_sessionIx > -1 ){
					fosp_sessionIx += fosp_sessionKW.length();
					userSession = request.getUri().substring( fosp_sessionIx , request.getUri().indexOf("%", fosp_sessionIx+2)  ) ;
					//System.out.println( "fosp_rand : " + fosp_rand );
				}
			}
			
		} catch (Exception e) {}
	}
	
	
	
	public FullHttpResponse responseForGetId(){
		// version cookies
		String eatv = "11-09-2013";
		
		FullHttpResponse response = null;
		try {
			//decode cookie string to CookieData
			setCookieDataFromRequest();
			
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
			Map<String, List<String>> params = queryStringDecoder.parameters();						
			
			String callback = NettyHttpUtil.getParamValue("callback",	params);
			String nid = NettyHttpUtil.getParamValue("nid", params);
			boolean noCache = NettyHttpUtil.getParamValue("nocache", params).equals("true");
			
			/*
			 * rand format
			 * a475fe783930cca5.1378884327.26.1378885082.1378885076.12.9999
			 * first time visit, number visit, current time, last time visit,number visit by id(30 minus) , locationId
			 */		
			userSession = SecurityUtil.decryptBeaconValue(userSession);	
			
			int rand_length = 0 ;
			try{
				rand_length = userSession.split("\\.").length ;
			}
			catch(Exception e){
				rand_length = 0;
			}
			
			if (StringUtil.isEmpty(userSession) || rand_length < 7 ) {
				if (StringUtil.isEmpty(userid)) {
					// get userid back up from request parameter (browser not accept cookies)
					userid = _getUserIdBK(request);
					if( "".equals(userid) ){
						// First time visit --> generateFospAID
						userid = CookieUtil.generateUserIdCookieString(request);
					}
				}
				//rand_locationId = LocationUtil.getLocationFromIp(ipAdress);
				userSession = CookieUtil.newSessionValue(userid, rand_locationId);			
			} else {
				updateFospSession();
			}			
						
					
					
			Map<Object, Object> obj = new HashMap<>();
			String session = userSession ;
			//System.out.println( "fosp_session : " + SecurityUtil.decryptBeaconValue(fosp_session) );
			if (USER_ID_COOKIE_NAME.equals(nid)) {
				obj.put("result", true);
				obj.put("nid", nid);
				obj.put("vid", userid);				
				obj.put("eatv", eatv);
				obj.put(SESSION_COOKIE_NAME, session);
			} else {
				obj.put("result", false);
			}
			
			if(callback.isEmpty()){
				response = StaticFileHandler.theJSONContent(new Gson().toJson(obj));
			} else {
				StringBuilder s = new StringBuilder();
				s.append(callback);
				s.append("(");
				s.append(new Gson().toJson(obj));
				s.append(");");
				response = StaticFileHandler.theJavaScriptContent(s.toString());
			}			
			//-------------------------------------------------------------------------

			if( ! noCache ){
				if(this.fospAidCookie == null){
					this.fospAidCookie = CookieUtil.createCookie(USER_ID_COOKIE_NAME,	userid, DEFAULT_DOMAIN, DEFAULT_PATH);
					this.fospAidCookie.setMaxAge(CookieUtil.COOKIE_AGE_10_YEARS);
					response.headers().add(SET_COOKIE,ServerCookieEncoder.encode(fospAidCookie));
				}			 
				
				
				
				Cookie sessionCookie = CookieUtil.createCookie(SESSION_COOKIE_NAME, URLEncoder.encode(userSession, "UTF-8"), DEFAULT_DOMAIN,	DEFAULT_PATH);
				sessionCookie.setMaxAge(CookieUtil.COOKIE_AGE_10_YEARS);
				response.headers().add(SET_COOKIE,ServerCookieEncoder.encode(sessionCookie));
							
				Cookie tracking_version = CookieUtil.createCookie(TRACKING_VERSION,URLEncoder.encode(eatv, "UTF-8"), DEFAULT_DOMAIN,DEFAULT_PATH);
				response.headers().add(SET_COOKIE,ServerCookieEncoder.encode(tracking_version));
				response.headers().set("P3P",P3P);
				
				String headerOrigin = request.headers().get("Origin");
				if(StringUtil.isNotEmpty(headerOrigin)){
					response.headers().set("Access-Control-Allow-Origin",headerOrigin);
					response.headers().set("Access-Control-Allow-Methods","GET");
					response.headers().set("Access-Control-Allow-Credentials","true");					
				}				
			}
			
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return NettyHttpUtil.theHttpContent(StringPool.BLANK);
	}
	
	
	
	private String _getUserIdBK(HttpRequest request){
		String userid_bk = "";
		int idx = request.getUri().indexOf("&id=");
		if( idx > -1 ){
			userid_bk = request.getUri().substring(idx+4);
		}
		return userid_bk;
	}
	
	boolean _isSafariBrowser(){
		String userAgent = request.headers().get("User-Agent");
		if( userAgent.indexOf("Chrome")==-1 && userAgent.indexOf("Safari")>-1 ){
			return true;
		}
		return false;
	}
}
