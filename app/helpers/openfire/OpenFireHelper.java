package helpers.openfire;

import java.util.Date;

import play.Logger;

public class OpenFireHelper {

	public static Date getDateFormLogTime(String logTimeString) {
		Date date = null;
		try {
			long logTime = Long.parseLong(removeLeadingZeros(logTimeString));
			//Logger.debug("parsing: " + removeLeadingZeros(logTimeString) + ", got " + logTime);
			date = new Date(logTime);
			//Logger.debug("parsing: " + removeLeadingZeros(logTimeString) + ", got " + date);
		} catch (NumberFormatException ex) {

		}
		return date;
	}

	public static String getLogTimeFromMillis(Long millis) {
		return addLeadingZeros(String.valueOf(millis));
	}

	private static String removeLeadingZeros(String str) {
		if (str == null) return str;
		String nStr = str;
		while (nStr.startsWith("0")) {
			nStr = nStr.substring(1);
		}
		return nStr;
	}

	private static String addLeadingZeros(String str) {
		String nStr = str;
		while (nStr.length() < 15) {
			nStr = "0" + nStr;
		}
		//Logger.debug("logTime is " + str + " => " + nStr + " length " + str.length());
		return nStr;
	}
}
