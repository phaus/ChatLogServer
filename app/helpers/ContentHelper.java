package helpers;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;


public class ContentHelper {

	public static String prepare(String content){
		return normalize(sanitize(content));
	}
	
	public static String normalize(String content){
		return content.replace("\n", "<br />");
	}
	
	public static String sanitize(String content){
		PolicyFactory policy = new HtmlPolicyBuilder()
	    .allowElements("a")
	    .allowUrlProtocols("img")
	    .allowUrlProtocols("br")
	    .allowAttributes("href").onElements("a")
	    .requireRelNofollowOnLinks()
	    .toFactory();
	return policy.sanitize(content);		
	}
}
