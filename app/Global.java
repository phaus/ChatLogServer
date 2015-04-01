import com.kenshoo.play.metrics.JavaMetricsFilter;

import play.GlobalSettings;
import play.api.mvc.EssentialFilter;

public class Global extends GlobalSettings {
	@Override
	public <T extends EssentialFilter> Class<T>[] filters() {
		return new Class[] { JavaMetricsFilter.class };
	}
}
