package controllers;

import com.typesafe.config.ConfigFactory;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.Application.*;

public class Application extends Controller {

	public final static String ISSUE_TRACKER_URL = ConfigFactory.load().getString("issue.tracker.url");

	public static Result index() {
		return ok(index.render());
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
	
	protected static String getQueryValue(String key, String defaultValue){
		return request().getQueryString(key) != null ? request().getQueryString(key) : defaultValue;
	}
}
