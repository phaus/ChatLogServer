package helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

public class DateHelper {
	private final static SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ssZ");
	
	public static DateTime getLogTimeForYearMonthDay(int year, int month, int day, Boolean inc) {
		DateTime dt = null;
		if (inc) {
			dt = new DateTime(year, month, day, 23, 59);
		} else {
			dt = new DateTime(year, month, day, 0, 0);			
		}
		return dt;
	}
	
	public static String getIsoDate(Date date){
		return ISO_8601_FORMAT.format(date);
	}
}
