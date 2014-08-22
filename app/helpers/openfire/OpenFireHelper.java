package helpers.openfire;

import java.util.Date;

public class OpenFireHelper {

	public static Date getDateFormLogTime(String logTimeString) {
		Date date = null;
		try {
			long logTime = Long.parseLong(logTimeString);
			date = new Date(logTime / 1000);
		} catch (NumberFormatException ex) {

		}
		return date;
	}
}
