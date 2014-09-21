package com.xasecure.audit.provider;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.dgc.VMID;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.helpers.LogLog;

public class MiscUtil {
	public static final String TOKEN_HOSTNAME          = "%hostname%";
	public static final String TOKEN_APP_INSTANCE      = "%app-instance%";
	public static final String TOKEN_CREATE_TIME_START = "%create-time:";
	public static final String TOKEN_CREATE_TIME_END   = "%";
	public static final String ESCAPE_STR = "\\";
	
	static VMID sJvmID = new VMID();
	
	public static String LINE_SEPARATOR = System.getProperty("line.separator");

	public static String replaceTokens(String str) {
		if(str == null) {
			return str;
		}

		str = replaceHostname(str);
		str = replaceAppInstance(str);
		str = replaceCreateTime(str);

		return str;
	}

	public static String replaceHostname(String str) {
		if(!str.contains(TOKEN_HOSTNAME)) {
			return str;
		}

		String hostName = null;

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException excp) {
			LogLog.warn("LocalFileLogBuffer", excp);
		}

		if(hostName == null) {
			hostName = "Unknown";
		}

		return str.replace(TOKEN_HOSTNAME, hostName);
	}
	
	public static String replaceAppInstance(String str) {
		if(!str.contains(TOKEN_APP_INSTANCE)) {
			return str;
		}

		String appInstance = Integer.toString(Math.abs(sJvmID.hashCode()));

		return str.replace(TOKEN_APP_INSTANCE, appInstance);
	}

	public static String replaceCreateTime(String str) {
		Date now = new Date();

        while(str.contains(TOKEN_CREATE_TIME_START)) {
            int tagStartPos = str.indexOf(TOKEN_CREATE_TIME_START);
            int tagEndPos   = str.indexOf(TOKEN_CREATE_TIME_END, tagStartPos + TOKEN_CREATE_TIME_START.length());

            if(tagEndPos <= tagStartPos) {
            	break;
            }

            String tag      = str.substring(tagStartPos, tagEndPos+1);
            String dtFormat = tag.substring(TOKEN_CREATE_TIME_START.length(), tag.lastIndexOf(TOKEN_CREATE_TIME_END));

            String replaceStr = "";

            if(dtFormat != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);

                replaceStr = sdf.format(now);
            }

            str = str.replace(tag, replaceStr);
        }

        return str;
	}

	public static void createParents(File file) {
		if(file != null) {
			String parentName = file.getParent();

			if (parentName != null) {
				File parentDir = new File(parentName);

				if(!parentDir.exists()) {
					parentDir.mkdirs();
				}
			}
		}
	}

	public static long getNextRolloverTime(long lastRolloverTime, long interval) {
		long now = System.currentTimeMillis() / 1000 * 1000; // round to second

		if(lastRolloverTime <= 0) {
			// should this be set to the next multiple-of-the-interval from start of the day?
			return now + interval;
		} else if(lastRolloverTime <= now) {
			long nextRolloverTime = now + interval;

			// keep it at 'interval' boundary
			long trimInterval = (nextRolloverTime - lastRolloverTime) % interval;

			return nextRolloverTime - trimInterval;
		} else {
			return lastRolloverTime;
		}
	}
	
	public static int parseInteger(String str, int defValue) {
		int ret = defValue;
		
		if(str != null) {
			try {
				ret = Integer.parseInt(str);
			} catch(Exception excp) {
				// ignore
			}
		}
		
		return ret;
	}
}

