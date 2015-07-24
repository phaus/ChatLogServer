package controllers;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.openfire.LogEntry;
import models.openfire.Room;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;

public class RoomStatistics extends Application {

	private final static int CACHE_TTL_IN_MINUTES = 10;
	private final static int CACHE_TTL_IN_SECONDS = 60 * CACHE_TTL_IN_MINUTES;
	private final static Room ALL_ROOMS = null;

	public static Result jsonShow(Long id, Integer days) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		String cacheKey = "roomStats-" + id + "-" + days;
		ObjectNode result = Json.newObject();
		ArrayNode results = (ArrayNode) Cache.get(cacheKey);
		if (results == null) {
			results = collectEntries(room, days, result.arrayNode());
			Cache.set(cacheKey, results, CACHE_TTL_IN_MINUTES);
		}
		result.put("entries", results);
		response().setHeader(CACHE_CONTROL, "max-age=" + CACHE_TTL_IN_SECONDS + ", public");
		response().setHeader(ETAG, String.valueOf(results.hashCode()));
		return ok(result);
	}

	public static Result jsonIndex(Integer days) {
		ObjectNode result = Json.newObject();
		String cacheKey = "roomsStats-" + days;
		ArrayNode results = (ArrayNode) Cache.get(cacheKey);
		if (results == null) {
			results = collectEntries(ALL_ROOMS, days, result.arrayNode());
			Cache.set(cacheKey, results, CACHE_TTL_IN_MINUTES);
		}
		result.put("entries", results);
		response().setHeader(CACHE_CONTROL, "max-age=" + CACHE_TTL_IN_SECONDS + ", public");
		response().setHeader(ETAG, String.valueOf(results.hashCode()));
		return ok(result);
	}

	private static ArrayNode collectEntries(Room room, Integer days, ArrayNode roomsJson) {
		DateTime from, to;
		to = DateTime.now();
		from = DateTime.now().minusDays(7);
		if (room == null) {
			for (int i = days; i >= 0; i--) {
				to = DateTime.now().minusDays(i);
				from = DateTime.now().minusDays(i + 1);
				roomsJson.add(LogEntry.getAllEntriesFromTo(from.getMillis(), to.getMillis()).size());
			}
		} else {
			for (int i = days; i >= 0; i--) {
				to = DateTime.now().minusDays(i);
				from = DateTime.now().minusDays(i + 1);
				roomsJson.add(room.getEntriesFromTo(from.getMillis(), to.getMillis()).size());
			}
		}
		return roomsJson;
	}

}
