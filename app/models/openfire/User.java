package models.openfire;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.Logger;
import play.cache.Cache;
import play.db.ebean.Model;

@Entity
@Table(name = "ofConParticipant")
public class User extends Model {
	
	@Transient
	private String username = null;

	@Column(name = "bareJID")
	public String jid;

	public final static int PAGE_SIZE = 200;

	public Integer getEntryCount() {
		return LogEntry.Finder
				.where()
				.startsWith("sender", getUsername() + "@")
				.findRowCount();
	}

	public List<LogEntry> getEntries(Integer page, String order) {
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder
								.where()
								.startsWith("sender", getUsername() + "@")
								.order("logTimeString " + order)
								.findPagingList(PAGE_SIZE)
								.getPage(p - 1).getList();
		return entries;
	}

	public String getUsername() {
		if (username == null) {
			username = jid.replace("@"+getDomain(jid), "").trim();
		}
		return username;
	}

	private String getDomain(String jid) {
		String[] parts =  jid.split("@");
		return parts[parts.length-1];
	}
	
	public static boolean exists(String uid) {
		Object exists = Cache.get(uid+"-exists");
		if(exists == null){
			Integer count = User.Finder.setDistinct(true)
					.where()
					.startsWith("bareJID", uid + "@")
					.findRowCount();
			exists = count > 0;
			Cache.set(uid+"-exists", exists, 60 * 60 * 24);
		}
		return exists != null;
	}

	public static List<User> list() {
		return User.Finder.setDistinct(true)
				.where()
				.ne("bareJID", null)
				.findList();
	}

	public static Finder<Long, User> Finder = new Finder<Long, User>("openfire", Long.class, User.class);
}
