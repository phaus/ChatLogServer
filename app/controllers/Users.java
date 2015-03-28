package controllers;

import helpers.EntryHelper;

import java.util.List;

import models.openfire.LogEntry;
import models.openfire.Room;
import models.openfire.User;
import play.mvc.Result;
import views.html.Users.browse;
import views.html.Users.index;

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
			
			// TODO adding an Instance of EntryHelper is not great, but will do it for now
			return ok(browse.render(user, entries, new EntryHelper(), prev, next, page, order));
		}
	}
	
	public static Result index(){
		List<User> users = User.Finder.all();
		return ok(index.render(users));
	}
}
