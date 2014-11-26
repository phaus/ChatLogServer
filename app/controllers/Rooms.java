package controllers;

import helpers.DateHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.openfire.LogEntry;
import models.openfire.Room;

import org.joda.time.DateTime;

import play.Logger;
import play.mvc.Result;
import views.html.Rooms.browse;
import views.html.Rooms.index;
import views.html.Rooms.show;

public class Rooms extends Application {
	public static Result index() {
		Map<String, Room> rooms = new TreeMap<String, Room>();
		for(Room room : Room.Finder.all()) {
			if(room != null && room.getLastEntry() != null){
				rooms.put(room.getLastEntryDate().toString(), room);
			}
			
		}
		return ok(index.render(rooms));
	}

	public static Result browse(Long id) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		return browseRoom(room);
	}

	public static Result browseWithName(String roomName) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		if (room == null) {
			return notFound("room with name " + roomName + " not found!");
		}
		return browseRoom(room);
	}

	public static Result showWithName(String roomName, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		if (room == null) {
			return notFound("room with name " + roomName + " not found!");
		}
		return showRoom(room, year, month, day);
	}

	public static Result show(Long id, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		return showRoom(room, year, month, day);
	}

	private static Result showRoom(Room room, Integer year, Integer month, Integer day) {
		DateTime from = DateHelper.getLogTimeForYearMonthDay(year, month, day, false);
		DateTime to = DateHelper.getLogTimeForYearMonthDay(year, month, day, true);
		List<LogEntry> entries = room.getEntriesFromTo(from.getMillis(), to.getMillis());
		return ok(show.render(room, entries, from, to));
	}

	public static Result feedWithName(String roomName) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		if (room == null) {
			return notFound("room with name " + roomName + " not found!");
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE/10 + 1;
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < div ? page + 1 : null;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		Logger.debug("feed page: " + page + ", prev: " + prev + ", next: " + next + ", div: " + div);
		List<LogEntry> entries = room.getEntries(page, order);		
		LogEntry lastEntry = room.getLastEntry();
		//routes.Application.show("1").toString();
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries));
	}
	
	public static Result feed(Long id) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}		
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE/10 + 1;
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < div ? page + 1 : null;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		Logger.debug("feed page: " + page + ", prev: " + prev + ", next: " + next + ", div: " + div);
		List<LogEntry> entries = room.getEntries(page, order);		
		LogEntry lastEntry = room.getLastEntry();
		//routes.Application.show("1").toString();
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries));
	}
	
	private static Result browseRoom(Room room) {
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < div ? page + 1 : null;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		Logger.debug("browse page: " + page + ", prev: " + prev + ", next: " + next + ", div: " + div);
		List<LogEntry> entries = room.getEntries(page, order);
		return ok(browse.render(room, entries, prev, next, page, order));
	}
}
