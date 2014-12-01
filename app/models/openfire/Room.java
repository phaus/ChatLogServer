package models.openfire;

import helpers.openfire.OpenFireHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.db.ebean.Model;
import play.libs.Json;
import play.mvc.Http.Request;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	private final static TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMAN);
	static {
		DATE_FORMAT.setTimeZone(TIMEZONE);
	}
	
	private static String LIST_RAW_SQL = "SELECT ofMucConversationLog.roomID, max(logTime) as mTime, ofMucRoom.roomID, ofMucRoom.serviceID, ofMucRoom.name, ofMucRoom.description, ofMucRoom.naturalName, ofMucRoom.roomPassword "
			+ "FROM ofMucConversationLog, ofMucRoom "
			+ "WHERE ofMucRoom.roomID = ofMucConversationLog.roomID "
			+ "GROUP BY ofMucConversationLog.roomID "
			+ "ORDER BY mTime DESC";
	private static RawSql LIST_RAW_SQL_QUERY = RawSqlBuilder  
												.parse(LIST_RAW_SQL)  
												.columnMapping("ofMucRoom.roomID",  "roomId")  
												.columnMapping("ofMucRoom.serviceID",  "serviceId")  
												.columnMapping("ofMucRoom.name",  "name")
												.columnMapping("ofMucRoom.description",  "description")
												.columnMapping("ofMucRoom.naturalName",  "title")
												.columnMapping("ofMucRoom.roomPassword",  "roomPassword")
												.columnMappingIgnore("ofMucConversationLog.roomID")
												.columnMappingIgnore("max(logTime)")
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
	
	public ObjectNode toJson(Request request){
		ObjectNode jsonRoom =  Json.newObject();
		jsonRoom.put("id", roomId);
		jsonRoom.put("name", title);
		jsonRoom.put("lastEntryDate", DATE_FORMAT.format(getLastEntryDate()));
		jsonRoom.put("entryCount", getEntryCount());
		jsonRoom.put("jabberId", getJabberId());
		jsonRoom.put("url", controllers.routes.Rooms.jsonWithName(name).absoluteURL(request, controllers.Application.REQUEST_SECURE).toString());
		return jsonRoom;
	}
}
