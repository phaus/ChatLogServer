package controllers;

import helpers.DateHelper;
import helpers.EntryHelper;

import java.util.List;

import models.openfire.LogEntry;
import models.openfire.Room;

import org.joda.time.DateTime;

import play.Logger;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.Rooms.browse;
import views.html.Rooms.index;
import views.html.Rooms.show;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Rooms extends Application {

	public static Result index() {
		List<Room> rooms = Room.listByDate();
		return ok(index.render(rooms));
	}

	public static Result feedAll() {
		List<LogEntry> entries = LogEntry.getEntries(1, "desc");
		LogEntry lastEntry = LogEntry.getLastEntry();
		return ok(views.xml.Rooms.feed_all.render(lastEntry, entries, request()));
	}

	public static Result jsonIndex() {
		List<Room> rooms = Room.listByDate();
		ObjectNode result = Json.newObject();
		ArrayNode roomsJson = result.arrayNode();
		for (Room room : rooms) {
			roomsJson.add(room.toJson(request()));
		}
		result.put("rooms", roomsJson);
		return ok(result);
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
		return redirect(
				routes.Rooms.browse(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE));
	}

	public static Result showWithName(String roomName, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		if (room == null) {
			return notFound("room with name " + roomName + " not found!");
		}
		return redirect(routes.Rooms.show(room.roomId, year, month, day).absoluteURL(request(),
				controllers.Application.REQUEST_SECURE));
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
		String cacheKey = "showRoom-" + String.format("%d-%d-%d-%d", room.roomId, year, month, day);
		List<LogEntry> entries = room.getEntriesFromTo(from.getMillis(), to.getMillis());
		Html out = null;
		if (DateTime.now().isAfter(to)) {
			out = (Html) Cache.get(cacheKey);
			Logger.info("laoding " + cacheKey + " from Cache!");
		}
		if (out == null) {
			out = show.render(room, entries, from, to);
			if (DateTime.now().isAfter(to)) {
				Cache.set(cacheKey, out);
				Logger.info(cacheKey + " saved to Cache!");
			}
		}
		return ok(out);
	}

	public static Result feedWithName(String roomName) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		if (room == null) {
			return notFound("room with name " + roomName + " not found!");
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < div ? page + 1 : null;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		Logger.debug("feed page: " + page + ", prev: " + prev + ", next: " + next + ", div: " + div);
		List<LogEntry> entries = room.getEntries(page, order);
		LogEntry lastEntry = room.getLastEntry();
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries, request()));
	}

	public static Result jsonWithName(String roomName) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with name " + roomName + " not found!");
			return notFound(result);
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
		Logger.debug("div: " + div + " room count: " + room.getEntryCount());
		Integer prev = page > 1 ? page - 1 : page;
		Integer next = page < div ? page + 1 : page;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";

		ObjectNode links = Json.newObject();
		links.put("index",
				routes.Rooms.jsonIndex().absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString());
		links.put("first", routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE)
				.toString());
		links.put("next",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + next);
		links.put("current",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + page);
		links.put("prev",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + prev);
		links.put("last",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + div);
		result.put("links", links);
		List<LogEntry> entries = room.getEntries(page, order);
		return ok(entriesAsJson(result, room, entries));
	}

	public static Result json(Long id) {
		Room room = Room.Finder.byId(id);
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with id " + id + " not found!");
			return notFound(result);
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
		Logger.debug("div: " + div + " room count: " + room.getEntryCount());
		Integer prev = page > 1 ? page - 1 : page;
		Integer next = page < div ? page + 1 : page;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";

		ObjectNode links = Json.newObject();
		links.put("index",
				routes.Rooms.jsonIndex().absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString());
		links.put("first", routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE)
				.toString());
		links.put("next",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + next);
		links.put("current",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + page);
		links.put("prev",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + prev);
		links.put("last",
				routes.Rooms.json(room.roomId).absoluteURL(request(), controllers.Application.REQUEST_SECURE).toString()
						+ "?page=" + div);
		result.put("links", links);

		List<LogEntry> entries = room.getEntries(page, order);
		return ok(entriesAsJson(result, room, entries));
	}

	public static Result jsonWithNameAndDate(String roomName, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.where().eq("name", roomName).findUnique();
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with name " + roomName + " not found!");
			return notFound(result);
		}
		return redirect(routes.Rooms.jsonWithDate(room.roomId, year, month, day).absoluteURL(request(),
				controllers.Application.REQUEST_SECURE));
	}

	public static Result jsonWithDate(Long id, Integer year, Integer month, Integer day) {
		Room room = Room.Finder.byId(id);
		ObjectNode result = Json.newObject();
		if (room == null) {
			result.put("error", "room with id " + id + " not found!");
			return notFound(result);
		}
		DateTime from = DateHelper.getLogTimeForYearMonthDay(year, month, day, false);
		DateTime to = DateHelper.getLogTimeForYearMonthDay(year, month, day, true);
		List<LogEntry> entries = room.getEntriesFromTo(from.getMillis(), to.getMillis());
		return ok(entriesAsJson(result, room, entries));
	}

	public static Result feed(Long id) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		Integer page = getPageFromRequest();
		Integer div = room.getEntryCount() / Room.PAGE_SIZE + 1;
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < div ? page + 1 : null;
		String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
		Logger.debug("feed page: " + page + ", prev: " + prev + ", next: " + next + ", div: " + div);
		List<LogEntry> entries = room.getEntries(page, order);
		LogEntry lastEntry = room.getLastEntry();
		return ok(views.xml.Rooms.feed.render(room, lastEntry, entries, request()));
	}

	private static ObjectNode entriesAsJson(ObjectNode result, Room room, List<LogEntry> entries) {
		ArrayNode entriesJson = result.arrayNode();
		for (LogEntry entry : entries) {
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

		// TODO adding an Instance of EntryHelper is not great, but will do it
		// for now
		return ok(browse.render(room, entries, new EntryHelper(), prev, next, page, order));
	}
}
