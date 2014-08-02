package rfx.server.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import rfx.server.configs.HttpServerConfigs;

public class LogUtil {
	public static final String ANSI_RESET = "\u001B[0m";

	public static final String ANSI_BLACK = "\u001B[30m";

	public static final String ANSI_RED = "\u001B[31m";

	public static final String ANSI_GREEN = "\u001B[32m";

	public static final String ANSI_YELLOW = "\u001B[33m";

	public static final String ANSI_BLUE = "\u001B[34m";

	public static final String ANSI_PURPLE = "\u001B[35m";

	public static final String ANSI_CYAN = "\u001B[36m";

	public static final String ANSI_WHITE = "\u001B[37m";

	static final int NTHREDS = 200;

	static String remoteHost = "";

	static final String LOG_EXT = ".log";

	static ThreadExecutor executor = new ThreadExecutor(500);

	static HttpServerConfigs httpServerConfigs = HttpServerConfigs.load();
	static {
		// logger.s
	}

	public static void i(Object tag, Object log) {
		if (StringPool.BLANK.equals(remoteHost)) {
			debug(tag, log + "", false);
		}
		else {
			
		}
	}

	static Logger logger = Logger.getRootLogger();

	public static void debug(Object tag, String log, boolean dumpToFile) {
		if (httpServerConfigs.getDebugModeEnabled() == 1) {
			if (!(tag instanceof String)) {
				tag = tag.getClass().getName();
			}
			if (StringPool.BLANK.equals(remoteHost)) {
				String time = "[" + DateTimeUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + "] ";
				System.out.println(time + ANSI_YELLOW + tag + ANSI_RESET + " : " + log);
				if (dumpToFile) {
					dumpToFile(tag + " : " + log, false);
				}
			}
		}
	}

	/**
	 * TODO realtime logging monitor (
	 * 
	 * @param Object tag
	 * @param String log
	 */
	public static void r(Object tag, String log) {

	}

	final static String FORMAT_DATE = "yyyy-MM-dd";
	final static String FORMAT_DATEHOUR = "yyyy-MM-dd-HH";

	final static String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";

	static void dumpToFile(final String s, final boolean isErrorMode) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {

					String prefix = isErrorMode ? "/analytics-error-log-" : "/analytics-debug-log-";

					Date d = new Date();
					String datetime = DateTimeUtil.formatDate(d, FORMAT_DATEHOUR);

					String time = "[" + DateTimeUtil.formatDate(d, FORMAT_TIME) + "] ";
					String data = time + s + "\n";

					String path = httpServerConfigs.getDebugLogFolderPath() + prefix + datetime + LOG_EXT;
					File file = new File(path);

					// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}

					// true = append file
					FileWriter fileWritter = new FileWriter(file, true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.write(data);
					bufferWritter.flush();
					bufferWritter.close();
					fileWritter.close();

				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static void dumpToFileIpLog(final String s) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				FileWriter fileWritter = null;
				try {
					String prefix = "/server-ip.log";
					Date d = new Date();
					String time = "[" + DateTimeUtil.formatDate(d, FORMAT_TIME) + "] ";
					String data = time + s + "\n";

					String date = DateTimeUtil.formatDate(d, FORMAT_DATE);
					
					String dirPath = httpServerConfigs.getDebugLogFolderPath() + "/" + date ;
					File dir = new File(dirPath);
					if( ! dir.isDirectory() ){
						dir.mkdir();
					}
					
					String path = dir.getAbsolutePath() + prefix;					
					File file = new File(path);					
					if (!file.exists()) {
						file.createNewFile();
					}

					// true = append file
					fileWritter = new FileWriter(file, true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.write(data);
					bufferWritter.flush();
					bufferWritter.close();
					fileWritter.close();

				}
				catch (IOException e) {
					e.printStackTrace();
				}finally {		
					if(fileWritter != null){
						try {
							fileWritter.close();
						} catch (IOException e) {}
					}
				}	
			}
		});
	}
	
	public static void dumpErrorLogData(final String log) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				FileWriter fileWritter = null;
				try {
					String prefix = "/server-http-error.log";

					String date = DateTimeUtil.formatDate(new Date(), FORMAT_DATE);
					
					String dirPath = httpServerConfigs.getDebugLogFolderPath() + "/" + date ;
					File dir = new File(dirPath);
					if( ! dir.isDirectory() ){
						dir.mkdir();
					}
					
					String path = dir.getAbsolutePath() + prefix;					
					File file = new File(path);					
					if (!file.exists()) {
						file.createNewFile();
					}

					// true = append file
					fileWritter = new FileWriter(file, true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
					bufferWritter.write(log);
					bufferWritter.flush();
					bufferWritter.close();
					fileWritter.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				} finally {		
					if(fileWritter != null){
						try {
							fileWritter.close();
						} catch (IOException e) {}
					}
				}	
			}
		});
	}
	
	public static void logDebug(String log) {
		FileWriter fileWritter = null;
		try {
			String date = DateTimeUtil.formatDate(new Date(), FORMAT_DATE);
			String prefix = "/server-http-debug.log";
			String dirPath = httpServerConfigs.getDebugLogFolderPath() + "/" + date ;
			File dir = new File(dirPath);
			if( ! dir.isDirectory() ){
				dir.mkdir();
			}
			
			String path = dir.getAbsolutePath() + prefix;					
			File file = new File(path);					
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			fileWritter = new FileWriter(file, true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(log);
			bufferWritter.flush();	
			bufferWritter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {		
			if(fileWritter != null){
				try {
					fileWritter.close();
				} catch (IOException e) {}
			}
		}			
	}
	


	public static void info(String log) {
		if (StringPool.BLANK.equals(remoteHost)) {
			String time = "[" + DateTimeUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + "] ";
			System.out.println(time + ANSI_BLUE + log + ANSI_RESET);
		}
	}

	public static void error(Object tag, Object log) {
		System.err.println(ANSI_RED + tag + ANSI_RESET + " : " + log);
		dumpToFile(tag + " : " + log, true);
	}
	
	public static void debug(Object tag, Object log) {
		System.out.println(ANSI_RED + tag + ANSI_RESET + " : " + log);
		dumpToFile(tag + " : " + log, true);
	}
	
	public static void error(Object log) {
		System.err.println(log);		
		dumpToFile(String.valueOf(log),true);
	}
}
