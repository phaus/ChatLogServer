package controllers;

import models.openfire.User;
import play.mvc.Controller;
import play.mvc.Result;

public class Users extends Controller {
	public static Result show(String uid) {
		User user = User.Finder.where().eq("username", uid).findUnique();
		if(user == null){
			return notFound("user with "+uid+" not found!");
		} else {
			return ok("found "+uid);
		}
	}
}
