package models.openfire;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "ofMucService")
public class RoomService extends Model {

	@Id
	@Column(name = "serviceID", columnDefinition = "bigint(20) NULL")
	public Long serviceId;
	@Column(name = "subdomain", columnDefinition = "varchar(255)")
	public String subdomain;
	@Column(name = "description", columnDefinition = "varchar(255) NULL")
	public String description;

	public static Finder<Long, RoomService> Finder = new Finder<Long, RoomService>("openfire", Long.class, RoomService.class);

	public String getDomain() {
		return subdomain + "." + JabberConfiguration.get("xmpp.domain");
	}
}
