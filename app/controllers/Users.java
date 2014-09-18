package controllers;

import java.util.List;

import models.openfire.LogEntry;
import models.openfire.Room;
import models.openfire.User;
import play.mvc.Result;
import views.html.Users.browse;

public class Users extends Application {
	
	public static Result browse(String uid) {
		User user = User.Finder.where().eq("username", uid).findUnique();
		if (user == null) {
			return notFound("user with " + uid + " not found!");
		} else {
	        Integer page = getPageFromRequest();
	        Integer div = user.getEntryCount() / Room.PAGE_SIZE + 1;
	        Integer prev = page > 1 ? page - 1 : null;
	        Integer next = page < div ? page + 1 : null;
	        String order = getQueryValue("order", "asc").equals("desc") ? "asc" : "desc";
			List<LogEntry> entries = user.getEntries(page, order);
			return ok(browse.render(user, entries, prev, next, page, order));
		}
	}
}
