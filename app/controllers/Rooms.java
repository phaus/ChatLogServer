package controllers;

import helpers.DateHelper;
import helpers.openfire.OpenFireHelper;

import java.util.List;

import org.joda.time.DateTime;

import models.openfire.LogEntry;
import models.openfire.Room;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Rooms.*;

public class Rooms extends Controller {
	public static Result index() {
		List<Room> rooms = Room.Finder.all();
		return ok(index.render(rooms));
	}

	public static Result browse(Long id) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
        Integer page = getPageFromRequest();
        Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
        Integer prev = page > 1 ? page - 1 : null;
        Integer next = page < div ? page + 1 : null;
        String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
        Logger.debug("page: "+page+", prev: "+prev+", next: "+next+", div: "+div);
		List<LogEntry> entries = room.getEntries(page, order);
		return ok(browse.render(room, entries, prev, next, page, order));
	}

	public static Result show(Long id, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		DateTime from = DateHelper.getLogTimeForYearMonthDay(year, month, day, false);
		DateTime to = DateHelper.getLogTimeForYearMonthDay(year, month, day, true);
		List<LogEntry> entries = room.getEntriesFromTo(from.getMillis(), to.getMillis());
		return ok(show.render(room, entries, from, to));
	}

	private static Integer getPageFromRequest() {
		Integer page = 1;
		try {
			if (request().getQueryString("page") != null) {
				page = Integer.parseInt(request().getQueryString("page"));
			}
		} catch (NumberFormatException ex) {

		}
		return page;
	}
	
	private static String getQueryValue(String key, String defaultValue){
		return request().getQueryString(key) != null ? request().getQueryString(key) : defaultValue;
	}
}
