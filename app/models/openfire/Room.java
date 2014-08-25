package models.openfire;

import helpers.openfire.OpenFireHelper;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.Logger;
import play.db.ebean.Model;

@Entity
@Table(name = "ofMucRoom")
public class Room extends Model {
	
	@Id
	@Column(name = "roomID", columnDefinition = "bigint(20) NULL")
	public Long roomId;
	@Column(name = "serviceID", columnDefinition = "bigint(20) NULL")
	public Long serviceId;
	public String name;
	public String description;
	@Column(name = "naturalName")
	public String title;
	@Column(name = "roomPassword")
	public String roomPassword;

	public final static int PAGE_SIZE = 200;
	
	public Date getLastEntryDate() {
		Date date = null;
		LogEntry entry = LogEntry.Finder.setMaxRows(1)
				.where()
				.eq("roomId", roomId)
				.order("logTimeString DESC")
				.findUnique();
		if (entry != null) {
			date = OpenFireHelper.getDateFormLogTime(entry.logTimeString);
		}
		return date;
	}

	public Integer getEntryCount(){
		return LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.findRowCount();
	}
	
	public List<LogEntry> getEntries(Integer page){
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.order("logTimeString DESC")
				.findPagingList(PAGE_SIZE).getPage(p-1).getList();
		return entries;
	}

	public List<LogEntry> getEntriesFromTo(Long from, Long to){
		String fromStr = OpenFireHelper.getLogTimeFromMillis(from);
		String toStr = OpenFireHelper.getLogTimeFromMillis(to);
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.ge("logTimeString", fromStr)
				.le("logTimeString", toStr)
				.order("logTimeString DESC").findList();
		return entries;
	}
	
	public String getJabberId(){
		Logger.debug("get service for "+serviceId);
		RoomService service = RoomService.Finder.byId(serviceId);
		if(service != null){
			return name+"@"+service.getDomain();
		}
		return "";
	}
	
	public static Finder<Long, Room> Finder = new Finder<Long, Room>("openfire", Long.class, Room.class);
}
