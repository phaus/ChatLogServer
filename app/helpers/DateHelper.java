package helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

public class DateHelper {
	private final static SimpleDateFormat RFC_3339_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	
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
		return RFC_3339_FORMAT.format(date);
	}
}
