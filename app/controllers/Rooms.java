package controllers;

import java.util.List;

import models.openfire.Room;
import play.mvc.Controller;
import play.mvc.Result;

public class Rooms extends Controller {
	public static Result index() {
		List<Room> room = Room.Finder.all();
		return ok("fooo");
	}
}
