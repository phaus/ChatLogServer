package models.openfire;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "ofVCard")
public class User extends Model {
	public String username;
	
	public static Finder<Long, User> Finder = new Finder<Long, User>("openfire", Long.class, User.class);
}
