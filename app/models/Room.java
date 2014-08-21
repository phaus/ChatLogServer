package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name = "ofMucRoom")
public class Room extends Model {
	@Id
	public Integer id;
	@Column(name = "roomID")
	public Integer roomId;
	public String name;
	public String description;
	public String title;
	public String roomPassword;

	public static Finder<Long, Room> Finder = new Finder<Long, Room>(
			Long.class, Room.class);
}
