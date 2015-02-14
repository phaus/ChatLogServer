package controllers;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.openfire.Room;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;

public class RoomStatistics extends Application {

	private final static int CACHE_TTL_IN_MINUTES = 60 * 10;

	public static Result jsonShow(Long id, Integer days) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		String cacheKey = "roomStats-" + id;
		ObjectNode result = Json.newObject();
		ArrayNode results = (ArrayNode) Cache.get(cacheKey);
		if (results == null) {
			results = collectEntries(room, days, result.arrayNode());
			Cache.set(cacheKey, results, CACHE_TTL_IN_MINUTES);
		}
		result.put("entries", results);
		return ok(result);
	}

	private static ArrayNode collectEntries(Room room, Integer days, ArrayNode roomsJson) {
		DateTime from, to;
		to = DateTime.now();
		from = DateTime.now().minusDays(7);
		for (int i = days; i >= 0; i--) {
			to = DateTime.now().minusDays(i);
			from = DateTime.now().minusDays(i + 1);
			roomsJson.add(room.getEntriesFromTo(from.getMillis(), to.getMillis()).size());
		}
		return roomsJson;
	}
}
