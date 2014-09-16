package helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import play.Logger;

public class ContentHelper {

	private String content;
	private final static Pattern URL_PATTERN = Pattern
			.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

	public ContentHelper(String content) {
		this.content = content;
	}

	public static String prepare(String content) {
		if (content == null) {
			return content;
		}
		ContentHelper ch = new ContentHelper(content);
		return ch.detectUsers().sanitize().detectLinks().normalize().toString();
	}

	public ContentHelper normalize() {
		content = content.replace("\n", "<br />");
		return this;
	}

	public ContentHelper sanitize() {
		PolicyFactory policy = new HtmlPolicyBuilder()
				.allowUrlProtocols("http")
				.allowUrlProtocols("https")
				.allowElements("a")
				.allowElements("img")			
				.allowElements("br")
				.allowAttributes("href")
				.onElements("a")
				.allowAttributes("src")
				.onElements("img")
				.requireRelNofollowOnLinks()
				.toFactory();
		content = policy.sanitize(content);
		return this;
	}

	public ContentHelper detectLinks() {
		String urlStr;
		StringBuilder contentBuilder = new StringBuilder();
		for (String line : content.split("\n")) {
			Matcher m = URL_PATTERN.matcher(line);
			while (m.find()) {
				urlStr = m.group();
				Logger.debug("found link |" + urlStr + "|");
				if(urlStr.contains("//www.youtube.com")) {
					line += embedYT(urlStr);
				} 
				line = line.replace(urlStr, embedLink(urlStr));					

			}
			contentBuilder.append(line.trim());
			contentBuilder.append("\n");
		}
		content = contentBuilder.toString();
		return this;
	}

	public ContentHelper detectUsers() {
		int start, end;
		String user;
		StringBuilder contentBuilder = new StringBuilder();
		for (String line : content.split("\n")) {
			line = line.trim();
			if (line.contains("@")) {
				start = line.indexOf(" @") + 1;
				if (start < 0 && line.startsWith("@"))
					start = 0;
				end = line.substring(start).indexOf(" ");
				if (end < 1)
					end = line.length();
				user = line.substring(start + 1, end).trim().toLowerCase();
				Logger.debug("found user |" + user + "| " + start + "-" + end);
				contentBuilder.append(line.substring(0, start)).append(" |" + user + "|").append(line.substring(end));
			} else {
				contentBuilder.append(line.trim());
			}
			contentBuilder.append("\n");
		}
		content = contentBuilder.toString();
		return this;
	}

	public String toString() {
		return this.content;
	}

	private String embedLink(String urlStr) {
		return "<a href=\"" + urlStr + "\" target=\"_new\">" + urlStr + "</a>";
	}

	private String embedYT(String urlStr) {
		Map<String, String> paras = getParametersFromUrl(urlStr);
		if(paras.containsKey("v")) {
			return "<iframe id=\"ytplayer\" type=\"text/html\" width=\"640\" height=\"390\" src=\"https://www.youtube.com/embed/"+paras.get("v")+"\" frameborder=\"0\" />";			
		}
		return "";
	}

	private static Map<String, String> getParametersFromUrl(String url) {
		Map<String, String> map = new HashMap<String, String>();
		if (url != null) {
			url = url.replace("&#61;", "=");
			String[] params = url.split("[&,?]");
			for (String param : params) {
				try {
					String name = param.split("=")[0];
					String value = param.split("=")[1];
					map.put(name, value);
				} catch (Exception e) {
					Logger.debug("No value for parameter "+param+" in "+url);
				}
			}
		}
		return map;
	}
}
