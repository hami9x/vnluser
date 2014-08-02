package rfx.server.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * date time utility class, support parse, format, get unix timestamp
 * 
 * @author Trieu.nguyen
 *
 */
public class DateTimeUtil {

	public final static String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	final static String GMT_DATE_TIME_FORMAT = "EEE, MMM d, yyyy hh:mm:ss z";
	
	static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	static final DateFormat DATE_NAME_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	static final DateFormat HOUR_NAME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:00:00");
	static final DateFormat DATE_FORMAT_FOR_DB = new SimpleDateFormat("yyyy-MM-dd");
	static final DateFormat DATEHOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH");
	static final DateFormat LOG_TIME_FORMATER = new SimpleDateFormat(LOG_TIME_FORMAT);
	

	public static String formatDate(Date d ){
		return DATE_FORMAT.format(d);
	}
	
	public static String formatDateName(Date d ){
		return DATE_FORMAT.format(d);
	}
	
	public static String formatHourName(Date d ){
		return HOUR_NAME_FORMAT.format(d);
	}
	
	public static long getTimestampFromDateHour(String d){
		try {
			return HOUR_NAME_FORMAT.parse(d).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static long getTimestampFromDate(String d){
		try {
			return DATE_NAME_FORMAT.parse(d).getTime();
		} catch (ParseException e) {
			System.err.println(d + " getTimestampFromDate " + e.getMessage());
		}
		return 0;
	}
	
	public static int getTimestampFromDate(java.sql.Date d){
		if(d != null){
			return (int) (d.getTime()/1000);
		}
		return 0;
	}
	
	public static String formatDate(Date d, String format ){
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(d);
	}
	
	public static String getDateStringForDb(Date d ){
		return DATE_FORMAT_FOR_DB.format(d);
	}
	
	public static Date parseDateStr(String str){
		try {
			return DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Date();
	}
	
	public static synchronized String getDateHourString(Date d ){
		return DATEHOUR_FORMAT.format(d);
	}
	
	public static int currentUnixTimestamp(){
		return (int) (System.currentTimeMillis() / 1000L);
	}
	
	public static String getLogTimeString(){
		return (new SimpleDateFormat(LOG_TIME_FORMAT)).format(new Date());
	}
	
	public static String getGMTDateTimeString() {		
		SimpleDateFormat sdf = new SimpleDateFormat(GMT_DATE_TIME_FORMAT);		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date());
	}
	
	public static String format(String format, Date date){
		return (new SimpleDateFormat(format)).format(date);
	}
}
