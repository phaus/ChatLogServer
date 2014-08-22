package models.openfire;

import helpers.openfire.OpenFireHelper;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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

	public String getEntryId() {
		return nickname + id;
	}

	public Date getDate() {
		return OpenFireHelper.getDateFormLogTime(logTimeString);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(getDate().toString());
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
}
