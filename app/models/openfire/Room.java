package models.openfire;

import helpers.openfire.OpenFireHelper;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.db.ebean.Model;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

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

	private static String LIST_RAW_SQL = "select distinct r.roomID, r.serviceID, r.name, r.description, r.naturalName, r.roomPassword "
			+ "FROM ofMucRoom r, ofMucConversationLog l WHERE l.roomID = r.roomID ORDER BY l.logTime ASC";
	private static RawSql LIST_RAW_SQL_QUERY = RawSqlBuilder  
												.parse(LIST_RAW_SQL)  
												.columnMapping("r.roomID",  "roomId")  
												.columnMapping("r.serviceID",  "serviceId")  
												.columnMapping("r.name",  "name")
												.columnMapping("r.description",  "description")
												.columnMapping("r.naturalName",  "title")
												.columnMapping("r.roomPassword",  "roomPassword")
												.create();  	
	@Transient
	public int lineCount = 0;
	
	public Date getLastEntryDate() {
		Date date = null;
		LogEntry entry = getLastEntry();
		if (entry != null) {
			date = OpenFireHelper.getDateFormLogTime(entry.logTimeString);
		}
		return date;
	}

	public LogEntry getLastEntry() {
		return LogEntry.Finder.setMaxRows(1)
				.where()
				.eq("roomId", roomId)
				.order("logTimeString DESC")
				.findUnique();	
	}
	
	public Integer getEntryCount() {
		return LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.findRowCount();
	}
	
	public List<LogEntry> getEntries(Integer page, String order) {
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.order("logTimeString "+order)
				.findPagingList(PAGE_SIZE).getPage(p-1).getList();
		return entries;
	}

	public List<LogEntry> getEntriesFromTo(Long from, Long to) {
		String fromStr = OpenFireHelper.getLogTimeFromMillis(from);
		String toStr = OpenFireHelper.getLogTimeFromMillis(to);
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.eq("roomId", roomId)
				.ge("logTimeString", fromStr)
				.le("logTimeString", toStr)
				.order("logTimeString ASC").findList();
		return entries;
	}
	
	public String getJabberId() {
		RoomService service = RoomService.Finder.byId(serviceId);
		if(service != null){
			return name+"@"+service.getDomain();
		}
		return "";
	}
	
	public String getUUID() {
		return UUID.nameUUIDFromBytes(name.getBytes()).toString();
	}
	
	public Integer updatedLineCount(int lineCount) {
		this.lineCount += lineCount;
		return this.lineCount;
	}

	
	public static Finder<Long, Room> Finder = new Finder<Long, Room>("openfire", Long.class, Room.class);

	public static List<Room> listByDate() {
		return Finder.setRawSql(LIST_RAW_SQL_QUERY).setDistinct(true).findList();		
	}
}
