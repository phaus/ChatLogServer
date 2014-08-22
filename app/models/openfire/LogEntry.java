package models.openfire;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "ofMucConversationLog")
public class LogEntry extends Model {
	@Id
	public Integer id;
	@Column(name = "roomID")
	public Integer roomId;
	public String sender;
	public String nickname;
	public String subject;
	public String body;
	@Column(name = "logTime")
	public String logTimeString;
	public Date logTime;

	public static Finder<Long, LogEntry> Finder = new Finder<Long, LogEntry>(
			Long.class, LogEntry.class);
}
