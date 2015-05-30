package helpers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.openfire.User;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import play.Logger;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.typesafe.config.ConfigFactory;

public class ContentHelper {

	private final static String HTTP_YOUTU_BE = "http://youtu.be";
	private final static String HTTPS_YOUTU_BE = "https://youtu.be";
	private final static String HTTP_WWW_YOUTUBE_COM = "http://www.youtube.com";
	private final static String HTTPS_WWW_YOUTUBE_COM = "https://www.youtube.com";
	private final static String HTTPS_WWW_YOUTUBE_COM_EMBED = "https://www.youtube.com/embed";
	private String content;
	private final static Pattern URL_PATTERN = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private final static int MAX_GET_TIMEOUT = 2500;
	private final static String USER_URL_TEMPLATE = ConfigFactory.load().getString("user.url.template");
	private final static String[] INVALID_USERNAME_PARTS = {":", ","};
	public ContentHelper(String content) {
		this.content = content;
	}

	public static String prepare(String content) {
		if (content == null) {
			return content;
		}
		ContentHelper ch = new ContentHelper(content.trim());
		return ch.renderHtml().detectUsers().sanitize().detectLinks().normalize().toString();
	}

	public ContentHelper normalize() {
		content = content.replace("\n", "<br />\n");
		return this;
	}

	public ContentHelper renderHtml() {
		content = content.replace("<", "&lt;").replace(">", "&gt;");
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
				.allowAttributes("target")
				.onElements("a")
				.allowAttributes("src")
				.onElements("img")
				.requireRelNofollowOnLinks()
				.toFactory();
		content = policy.sanitize(content);
		return this;
	}

	public ContentHelper detectLinks() {
		String urlStr, oldLine;
		StringBuilder contentBuilder = new StringBuilder();
		for (String line : content.split("\n")) {
			oldLine = line.trim();
			Matcher m = URL_PATTERN.matcher(line);
			while (m.find()) {
				urlStr = m.group();
				Logger.debug("found link |" + urlStr + "|");
				line = line.replace(urlStr, embedLink(urlStr.replace("&#61;", "=")));		
				if(urlStr.startsWith(HTTP_WWW_YOUTUBE_COM) || urlStr.startsWith(HTTPS_WWW_YOUTUBE_COM)) {
					Logger.debug("YOUTUBE_COM: "+line);
					line += "<br />"+embedYT(urlStr.replace("&#61;", "="));
				}
				if(urlStr.startsWith(HTTP_YOUTU_BE) || urlStr.startsWith(HTTPS_YOUTU_BE)) {
					Logger.debug("YOUTU_BE: "+line);
					line += "<br />"+embedYT(convertShortYTUrl(urlStr));					
				}
				if(oldLine.equals(urlStr)) {
					line += "<br />"+embedImage(urlStr.replace("&#61;", "="));
				}
			}
			contentBuilder.append(line.trim());
			contentBuilder.append("\n");
		}
		content = contentBuilder.toString();
		return this;
	}

	public ContentHelper detectUsers() {
		StringBuilder contentBuilder = new StringBuilder();
		for (String line : content.split("\n")) {
			contentBuilder = detectUser(contentBuilder, line);
		}
		content = contentBuilder.toString();
		return this;
	}
	
	private StringBuilder detectUser(StringBuilder contentBuilder, String line){
		int start, end;
		String user, cleanuser;
		if (line.contains(" @") || line.startsWith("@")) {
			start = line.indexOf(" @") + 1;
			if (start < 0 || line.startsWith("@"))
				start = 0;
			end = Math.min(line.substring(start).indexOf(" ")+start, line.length());
			if (end < start)
				end = line.length();
			if(end > start){
				user = line.substring(start + 1, end);
				cleanuser = user.trim().toLowerCase(); 
				for(String part : INVALID_USERNAME_PARTS){
					cleanuser = cleanuser.replace(part, "");
				}
				Logger.debug("found user |" + user + "| " + start + "-" + end);
				if(User.exists(cleanuser)) {
					contentBuilder.append(line.substring(0, start)).append("<a href=\""+USER_URL_TEMPLATE.replace(":uid", cleanuser)).append("\">@"+user+"</a> ");					
				} else {
					contentBuilder.append(line.trim());
				}					
			}else {
				Logger.warn("false user in: "+line.trim());
				contentBuilder.append(line.trim());
			}			
			return detectUser(contentBuilder, line.substring(Math.min(end+1, line.length())));
		} else {
			contentBuilder.append(line.trim());
		}
		return contentBuilder;
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
			return "<iframe id=\"ytplayer\" type=\"text/html\" width=\"640\" height=\"390\" src=\""+buildEmbeddedYTUrl(paras)+"\" frameborder=\"0\"></iframe>";			
		}
		return "";
	}

	private String embedImage(String urlStr) {
		try {
			urlStr = URLEncoder.encode(urlStr, "UTF-8");
			Promise<WSResponse> response = WS.url(urlStr).get();
			try {
				if (response.get(MAX_GET_TIMEOUT).getHeader("Content-Type").startsWith("image")) {
					return "<img src=\"" + urlStr + "\" />";
				}
			} catch (Exception ex) {
				Logger.warn(ex.getLocalizedMessage());
			}
		} catch (Exception ex) {
			Logger.warn(ex.getLocalizedMessage());
		}

		return "";
	}

	private static String buildEmbeddedYTUrl(Map<String, String> paras){
		StringBuilder sb = new StringBuilder(HTTPS_WWW_YOUTUBE_COM_EMBED);
		if(paras.containsKey("v")){
			sb.append("/"+paras.get("v"));
		}
		if(paras.containsKey("t")){
			sb.append("?start="+paras.get("t").replace("s", ""));
		}		
		return sb.toString();
	}
	
	private static String convertShortYTUrl(String shortYTUrl) {
		Map<String, String> map = new HashMap<String, String>();
		shortYTUrl = shortYTUrl.replace(HTTPS_YOUTU_BE, "").replace(HTTP_YOUTU_BE, "").replace("&#61;", "=").trim();
		String params[] = shortYTUrl.split("[/,?]");
		for (String param : params) {
			try {
				if(param.split("=").length == 2) {
					map.put(param.split("=")[0],  param.split("=")[1]);	
				} else if(!param.isEmpty()) {
					map.put("v",  param);	
				}				
			} catch (Exception e) {
				Logger.warn("No value for parameter "+param+" in shortYTUrl "+shortYTUrl);
			}
		}
		return HTTPS_WWW_YOUTUBE_COM+"/watch?v="+map.get("v")+"&t="+map.get("t");
	}
	
	private static Map<String, String> getParametersFromUrl(String url) {
		Map<String, String> map = new HashMap<String, String>();
		if (url != null) {
			String[] params = url.trim().split("[&,?]");
			for (String param : params) {
				try {
					String name = param.split("=")[0];
					String value = param.split("=")[1];
					map.put(name, value);
				} catch (Exception e) {
					Logger.warn("No value for parameter "+param+" in "+url);
				}
			}
		}
		return map;
	}
}
