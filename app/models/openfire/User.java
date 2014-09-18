package models.openfire;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "ofVCard")
public class User extends Model {
	public String username;
	
	public final static int PAGE_SIZE = 200;
	
	public Integer getEntryCount(){
		return LogEntry.Finder
				.where()
				.startsWith("sender", username+"@")
				.findRowCount();
	}	
	
	public List<LogEntry> getEntries(Integer page, String order){
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.startsWith("sender", username+"@")
				.order("logTimeString "+order)
				.findPagingList(PAGE_SIZE).getPage(p-1).getList();
		return entries;	
	}
	public static Finder<Long, User> Finder = new Finder<Long, User>("openfire", Long.class, User.class);
}
