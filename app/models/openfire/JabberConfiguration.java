package models.openfire;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import models.openfire.LogEntry.LogEntryId;
import play.db.ebean.Model;

@Entity
@Table(name = "ofProperty")
public class JabberConfiguration extends Model {

	@EmbeddedId
	public JabberConfigurationId id;

	public String name;
	@Column(name = "propValue")
	public String propValue;

	public static Finder<Long, JabberConfiguration> Finder = new Finder<Long, JabberConfiguration>("openfire",
			Long.class, JabberConfiguration.class);

	public static String get(String name) {
		JabberConfiguration configuration = JabberConfiguration.Finder.where().eq("name", name).findUnique();
		return configuration != null ? configuration.propValue : "";
	}

	@Embeddable
	public class JabberConfigurationId {
		@Column(name = "name", columnDefinition = "varchar(100)")
		public String name;

		public int hashCode() {
			return 1013 * name.hashCode();
		}

		public String toString() {
			return name;
		}

		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof JabberConfigurationId))
				return false;

			JabberConfigurationId other = (JabberConfigurationId) obj;
			return name.equals(other.name);
		}
	}
}
