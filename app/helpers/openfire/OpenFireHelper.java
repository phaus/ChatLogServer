package helpers.openfire;

import java.util.Date;

import play.Logger;

public class OpenFireHelper {

	public static Date getDateFormLogTime(String logTimeString) {
		Date date = null;
		try {
			long logTime = Long.parseLong(removeLeadingZeros(logTimeString));
			Logger.debug("parsing: " + removeLeadingZeros(logTimeString) + ", got " + logTime);
			date = new Date(logTime);
			Logger.debug("parsing: " + removeLeadingZeros(logTimeString) + ", got " + date);
		} catch (NumberFormatException ex) {

		}
		return date;
	}

	public static String getLogTimeFromMillis(Long millis) {
		return addLeadingZeros(String.valueOf(millis));
	}

	private static String removeLeadingZeros(String str) {
		while (str.startsWith("0")) {
			str = str.substring(1);
		}
		return str;
	}

	private static String addLeadingZeros(String str) {
		while (str.length() < 15) {
			str = "0" + str;
		}
		Logger.debug("logTime is " + str + " lenght " + str.length());
		return str;
	}
}
