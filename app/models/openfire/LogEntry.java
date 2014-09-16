package models.openfire;

import helpers.ContentHelper;
import helpers.openfire.OpenFireHelper;

import java.text.SimpleDateFormat;
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

import play.Logger;
import play.db.ebean.Model;

@Entity
@Table(name = "ofMucConversationLog")
public class LogEntry extends Model {

	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
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
			return logTimeString + roomId;
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

	public String getEntryId() {
		return getSenderName() + id;
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

	public String toTableRow(Room room) {
		return getTableRows(room, this);
	}

	public static String getTableRows(Room room, LogEntry entry) {
		StringBuilder sb = new StringBuilder();
		String parts[] = entry.body != null ? entry.body.split("\n") : new String[] {};
		for (int i = 0; i < parts.length; i++) {
			int line = room.lineCount + i + 1;
			sb.append("<tr  class=\"entry");
			if (i == 0) {
				sb.append(" head");
			}
			sb.append("\">");
			sb.append("<th class=\"tiny\">");
			sb.append("<a data-line=\"" + line + "\" id=\"L" + line + "\" name=\"L" + line + "\" href=\"#L" + line + "\">#" + line + "</a>");
			sb.append("</th>");
			if (i == 0) {
				sb.append("<td rowspan=\"" + parts.length + "\" class=\"top narrow\">" + entry.getSenderName() + "</td>");
				sb.append("<td>");
				if (entry.subject != null) {
					sb.append("<em>" + ContentHelper.prepare(entry.subject) + "</em>");
				}
				sb.append(ContentHelper.prepare(parts[i]));
				sb.append("</td>");
				sb.append("<td rowspan=\"" + parts.length + "\" class=\"top narrow\">"
						+ TIME_FORMAT.format(entry.getDate()) + "</td>");
			} else {
				sb.append("<td>");
				sb.append(ContentHelper.prepare(parts[i]));
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		room.updatedLineCount(parts.length);
		return sb.toString();
	}

	public static Finder<Long, LogEntry> Finder = new Finder<Long, LogEntry>("openfire", Long.class, LogEntry.class);
}
