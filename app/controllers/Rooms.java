package controllers;

import helpers.DateHelper;
import helpers.EntryHelper;

import java.util.List;

import models.openfire.LogEntry;
import models.openfire.Room;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import views.html.Rooms.browse;
import views.html.Rooms.index;
import views.html.Rooms.show;

public class Rooms extends Application {
	public static Result index() {
		List<Room> rooms = Room.listByDate();
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
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries, request()));
	}
	
	public static Result jsonWithName(String roomName){
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with name " + roomName + " not found!");
			return notFound(result);
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE/10 + 1;
		Integer prev = page > 1 ? page - 1 : page;
		Integer next = page < div ? page + 1 : page;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		
		ObjectNode links = Json.newObject();
		links.put("first", routes.Rooms.jsonWithName(room.name).absoluteURL(request()).toString());	
		links.put("next", routes.Rooms.jsonWithName(room.name).absoluteURL(request()).toString()+"?page="+next);
		links.put("prev", routes.Rooms.jsonWithName(room.name).absoluteURL(request()).toString()+"?page="+prev);
		links.put("last", routes.Rooms.jsonWithName(room.name).absoluteURL(request()).toString()+"?page="+div);
		result.put("links", links);
		
		List<LogEntry> entries = room.getEntries(page, order);
		return ok(entriesAsJson(result, room, entries));
	}
	
	public static Result jsonWithNameAndDate(String roomName, Integer year, Integer month, Integer day){
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with name " + roomName + " not found!");
			return notFound(result);
		}	
		return ok("");
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
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries, request()));
	}
	
	private static ObjectNode entriesAsJson(ObjectNode result, Room room, List<LogEntry> entries){
		ArrayNode entriesJson = result.arrayNode();
		for(LogEntry entry : entries){
			entriesJson.add(EntryHelper.getJson(room, entry, request()));
		}
		result.put("entries", entriesJson);
		return result;
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
