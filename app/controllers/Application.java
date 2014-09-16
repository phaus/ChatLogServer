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

}
