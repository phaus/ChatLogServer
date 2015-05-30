package models.openfire;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import controllers.Application;
import play.db.ebean.Model;

@Entity
@Table(name = "ofPubsubNode")
public class User extends Model {

	private static String DOMAIN = null;
	
	@Transient
	public String username = null;

	@Column(name = "parent")
	public String jid;

	public final static int PAGE_SIZE = 200;

	public Integer getEntryCount() {
		return LogEntry.Finder.where().startsWith("sender", getUsername() + "@").findRowCount();
	}

	public List<LogEntry> getEntries(Integer page, String order) {
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder.where().startsWith("sender", getUsername() + "@")
				.order("logTimeString " + order).findPagingList(PAGE_SIZE).getPage(p - 1).getList();
		return entries;
	}

	public String getUsername() {
		if (username == null) {
			username = jid.replace("@"+getDomain(jid), "").trim();
		}
		return username;
	}

	private String getDomain(String jid) {
		if(DOMAIN == null){
			String[] parts =  jid.split("@");
			DOMAIN = parts[parts.length-1];
		}
		return DOMAIN;
	}
	
	public static boolean exists(String uid) {
		User user = User.Finder.setDistinct(true).where().startsWith("parent", uid + "@").findUnique();
		return user != null && user.getUsername().length() > 0;
	}

	public static List<User> list() {
		return User.Finder.setDistinct(true).where().ne("parent", null).findList();
	}

	public static Finder<Long, User> Finder = new Finder<Long, User>("openfire", Long.class, User.class);
}
