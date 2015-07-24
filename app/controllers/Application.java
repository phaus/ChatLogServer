package controllers;

import com.typesafe.config.ConfigFactory;

import models.openfire.LogEntry;
import models.openfire.Room;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Application.index;
import views.html.Application.show;

public class Application extends Controller {

	public final static String ISSUES_URL = ConfigFactory.load().getString("issues.url");
	public final static String DOCS_URL = ConfigFactory.load().getString("docs.url");
	public final static boolean REQUEST_SECURE = getRequestSecure();
	
	public static Result index() {
		return ok(index.render());
	}

	public static Result show(String id) {
		String[] parts = id.split("_");
		if (parts.length < 3) {
			return notFound("Entry with id " + id + " not found!");
		}
		Room room = Room.Finder.where().eq("roomId", parts[1]).findUnique();
		LogEntry entry = LogEntry.findEntry(room, parts[0], parts[2]);
		if(room == null || entry == null){
			return notFound("Entry with id " + id + " not found!");
		}
		return ok(show.render(room, entry));
	}

	protected static Integer getPageFromRequest() {
		Integer page = 1;
		try {
			if (request().getQueryString("page") != null) {
				page = Integer.parseInt(request().getQueryString("page"));
			}
		} catch (NumberFormatException ex) {

		}
		return page;
	}

	protected static String getQueryValue(String key, String defaultValue) {
		return request().getQueryString(key) != null ? request().getQueryString(key) : defaultValue;
	}
	
	private static Boolean getRequestSecure() {
		if(ConfigFactory.load().hasPath("request.secure")){
			return Boolean.parseBoolean(ConfigFactory.load().getString("request.secure"));
		}
		return false;
	}
	
}
