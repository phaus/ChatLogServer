package controllers;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.openfire.Room;
import play.libs.Json;
import play.mvc.Result;

public class RoomStatistics extends Application {

	public static Result jsonShow(Long id, Integer days) {
		Room room = Room.Finder.byId(id);
		if (room == null) {
			return notFound("room with id " + id + " not found!");
		}
		ObjectNode result = Json.newObject();
		result.put("entries", collectEntries(room, days, result.arrayNode()));
		return ok(result);
	}

	private static ArrayNode collectEntries(Room room, Integer days, ArrayNode roomsJson) {
		DateTime from, to;
		to = DateTime.now();
		from = DateTime.now().minusDays(7);
		for (int i = 0; i < days; i++) {
			to = DateTime.now().minusDays(i);
			from = DateTime.now().minusDays(i + 1);
			roomsJson.add(room.getEntriesFromTo(from.getMillis(), to.getMillis()).size());
		}
		return roomsJson;
	}
}
