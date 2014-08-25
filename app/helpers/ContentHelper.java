package helpers;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import play.Logger;


public class ContentHelper {

	private String content;

	public ContentHelper(String content) {
		this.content = content;
	}

	public static String prepare(String content) {
		if (content == null) {
			return content;
		}
		ContentHelper ch = new ContentHelper(content);
		return ch.detectUsers().sanitize().normalize().toString();
	}

	public ContentHelper normalize() {
		content = content.replace("\n", "<br />");
		return this;
	}
	
	public ContentHelper sanitize(){
		PolicyFactory policy = new HtmlPolicyBuilder()
	    .allowElements("a")
	    .allowUrlProtocols("img")
	    .allowUrlProtocols("br")
	    .allowAttributes("href").onElements("a")
	    .requireRelNofollowOnLinks()
	    .toFactory();
		content =  policy.sanitize(content);
		return this;	
	}
	
	public ContentHelper detectUsers(){
		int start, end;
		String user;
		StringBuilder contentBuilder = new StringBuilder();
		for(String line : content.split("\n")){
			line = line.trim();
			if(line.contains("@")){
				start = line.indexOf(" @") + 1;
				if(start < 0 && line.startsWith("@"))start = 0;
				end = line.substring(start).indexOf(" ");
				if(end < 1) end = line.length();
				user = line.substring(start+1, end).trim().toLowerCase();
				Logger.debug("found user |"+user+"| "+start+"-"+end);
				contentBuilder.append(line.substring(0, start)).append(" |"+user+"|").append(line.substring(end));
			} else {
				contentBuilder.append(line.trim());
			}
			contentBuilder.append("\n");
		}
		content = contentBuilder.toString();
		return this;
	}
	
	public String toString(){
		return this.content;
	}
}
