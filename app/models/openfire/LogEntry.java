package models.openfire;

import helpers.DateHelper;
import helpers.openfire.OpenFireHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import play.db.ebean.Model;

@Entity
@Table(name = "ofMucConversationLog")
public class LogEntry extends Model {

	@EmbeddedId
	public LogEntryId id;
	@Column(name = "roomID", columnDefinition = "bigint(20) NULL")
	public Long roomId;
	@Column(name = "logTime")
	public String logTimeString;
	public String sender;
	public String nickname;
	public String subject;
	public String body;
	
	public final static int PAGE_SIZE = 200;
	
	@Transient
	public DateTime logTime = null;

	@Embeddable
	public class LogEntryId {
		@Column(name = "roomID", columnDefinition = "bigint(20) NULL")
		public Long roomId;
		@Column(name = "logTime")
		public String logTimeString;

		public int hashCode() {
			return 1013 * (logTimeString.hashCode()) ^ 1009 * (roomId.hashCode());
		}

		public String toString() {
			return roomId + "_" + logTimeString;
		}

		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof LogEntryId))
				return false;

			LogEntryId other = (LogEntryId) obj;
			return roomId.equals(other.roomId) && logTimeString.equals(other.logTimeString);
		}
	}

	public String getSenderName() {
		String parts[] = this.sender != null ? sender.split("@", 2) : null;
		return parts != null && parts.length > 0 ? parts[0].trim() : this.sender;
	}

	public String getTitle() {
		if(this.subject != null) {
			return this.subject;
		}
		return getWords(this.body, 10);
	}
	
	public String getUpdated() {
		return DateHelper.getIsoDate(getDate());
	}
	
	public String getRoomName() {
		Room room = Room.Finder.byId(roomId);
		return room != null ? room.name : String.valueOf(roomId);
	}

	public String getEntryId() {
		return getSenderName() + "_" + id;
	}

	public int getLineCount() {
		return body == null ? 1 : body.split("\n").length;
	}

	public List<String> getLines() {
		return body == null ? new LinkedList<String>() : Arrays.asList(body.split("\n"));
	}

	public DateTime getDateTime() {
		if (logTime == null) {
			logTime = new DateTime(OpenFireHelper.getDateFormLogTime(logTimeString));
		}
		return logTime;
	}

	public Date getDate() {
		return getDateTime().toDate();
	}

	public int getYear() {
		return getDateTime().getYear();
	}

	public int getMonth() {
		return getDateTime().getMonthOfYear();
	}

	public int getDay() {
		return getDateTime().getDayOfMonth();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(getDateTime().toString());
		sb.append(" ").append(nickname).append(": ");
		if (subject != null) {
			sb.append("[" + subject + "]");
		}
		if (body != null) {
			sb.append(" ").append(body);
		}
		return sb.toString();
	}
	
	public static Finder<Long, LogEntry> Finder = new Finder<Long, LogEntry>("openfire", Long.class, LogEntry.class);
	
	/**
	 * Finds an Entry for a TimeString in a specific Room.
	 * @param room
	 * @param sender
	 * @param logTimeString
	 * @return
	 */
	public static LogEntry findEntry(Room room, String sender, String logTimeString){
		return LogEntry.Finder.where().eq("roomId", room.roomId).eq("logTimeString", logTimeString).findUnique();
	}
	
	public static List<LogEntry> getEntries(Integer page, String order) {
		int p = page != null ? page : 1;
		List<LogEntry> entries = LogEntry.Finder
				.where()
				.order("logTimeString "+order)
				.findPagingList(PAGE_SIZE).getPage(p-1).getList();
		return entries;
	}
	
	public static LogEntry getLastEntry() {
		return LogEntry.Finder.setMaxRows(1)
				.where()
				.order("logTimeString DESC")
				.findUnique();	
	}
	
	private String getWords(String content, int count){
		StringBuilder sb = new StringBuilder();
		if(content != null){
			String parts[] = content.split(" ");
			for(int i=0; i < Math.min(parts.length, count); i++){
				sb.append(parts[i]).append(" ");
			}
		}
		return sb.toString();
	}
}
