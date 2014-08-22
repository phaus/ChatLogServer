package controllers;

import java.util.List;

import org.joda.time.DateTime;

import models.openfire.LogEntry;
import models.openfire.Room;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Rooms.*;

public class Rooms extends Controller {
	public static Result index() {
		List<Room> rooms = Room.Finder.all();
        return ok(index.render(rooms));
	}

	public static Result show(Long id) {
		Room room = Room.Finder.byId(id);
		if(room == null){
			return notFound("room with id "+id+" not found!");
		}
		DateTime from = new DateTime(room.getLastEntryDate());
		DateTime to = from.plusDays(1);
		List<LogEntry> entries = room.getEntries(null);
		return ok(show.render(room, entries));
	}
}
