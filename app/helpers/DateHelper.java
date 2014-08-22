package helpers;

import org.joda.time.DateTime;

import play.Logger;

public class DateHelper {
	public static DateTime getLogTimeForYearMonthDay(int year, int month, int day, Boolean inc) {
		DateTime dt = null;
		if (inc) {
			dt = new DateTime(year, month, day, 23, 59);
		} else {
			dt = new DateTime(year, month, day, 0, 0);			
		}
		//Logger.debug(" " + day + "." + month + "." + year + (inc ? " incl day" : " excl day") + " => " + dt + ", " + dt.getMillis());
		return dt;
	}
}
