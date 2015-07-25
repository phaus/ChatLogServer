package helpers;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import play.libs.Json;
import play.mvc.Http.Request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import models.openfire.LogEntry;
import models.openfire.Room;
import models.openfire.User;

public class EntryHelper {
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private final static TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
	private final static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMAN);
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
	static {
		DATE_TIME_FORMAT.setTimeZone(TIMEZONE);
		DATE_FORMAT.setTimeZone(TIMEZONE);
	}
	
	private final static String USER_AVATAR_URL_TEMPLATE = ConfigFactory.load().getString("user.avatar.url.template");
	private final static String USER_URL_TEMPLATE = ConfigFactory.load().getString("user.url.template");
	
	private String lastDay = "";
	private String lastRoom = "";
	
	public static ObjectNode getJson(Room room, LogEntry entry, Request request){
		ObjectNode entryJson = Json.newObject();
		entryJson.put("id", entry.getEntryId());
		entryJson.put("sender", entry.getSenderName());
		entryJson.put("subject", entry.subject);
		entryJson.put("content", entry.body);
		entryJson.put("date", DATE_TIME_FORMAT.format(entry.getDate()));
		entryJson.put("link", controllers.routes.Application.show(entry.getEntryId()).absoluteURL(request, controllers.Application.REQUEST_SECURE));
		return entryJson;
	}
	
	public String checkDayChange(LogEntry entry, int columns){
		if(lastDay.isEmpty() || !lastDay.equals(DATE_FORMAT.format(entry.getDate()))) {
			lastDay = DATE_FORMAT.format(entry.getDate());
			lastRoom = "";
			return "<tr><th class=\"day-header\" colspan=\""+columns+"\"><h3>"+lastDay+"</h3></th></tr>";
		}
		return "";
	}
	
	public String checkRoomChange(LogEntry entry, int columns, String link){
		if(lastRoom.isEmpty() || !lastRoom.equals(entry.getRoomName())) {
			lastRoom = entry.getRoomName();
			return link != null ? "<tr><th class=\"room-header\" colspan=\""+columns+"\"><h4><a href=\""+link+"\">"+lastRoom+"</a></h4></th></tr>" : "<tr><th class=\"room-header\" colspan=\""+columns+"\"><h4>"+lastRoom+"</h4></th></tr>";
		} 
		return "";
	}
	
	public static String getTableRows(Room room, LogEntry entry) {
		StringBuilder sb = new StringBuilder();
		
		if(entry.body != null){
			String parts[] =  entry.body.split("\n");
			for (int i = 0; i < parts.length; i++) {
				int line = room.lineCount + i + 1;
				sb.append("<tr data-line=\"" + line + "\" class=\"entry");
				if (i == 0) {
					sb.append(" head\" id=\""+entry.getEntryId());
				}
				sb.append("\">");
				sb.append("<th class=\"tiny\">");
				sb.append("<a data-link=\"line-link\" id=\"L" + line + "\" name=\"L" + line + "\" href=\"#L" + line + "\">#" + line + "</a>");
				sb.append("</th>");
				if (i == 0) {
					sb.append("<td class=\"top narrow\">");
					sb.append(decorateIcon(entry.getSenderName()));
					sb.append("</td>");
					sb.append("<td>");
					sb.append("<div class=\"content\">");
					if (entry.subject != null) {
						sb.append("changed topic to <h4>" + entry.subject + "</h4>");
					} else {
						sb.append(ContentHelper.prepare(parts[i]));					
					}
					sb.append("</div>");
					sb.append("</td>");
					sb.append("<td class=\"top narrow\">" + TIME_FORMAT.format(entry.getDate()) + "</td>");
				} else {
					sb.append("<td></td>");
					sb.append("<td>");
					sb.append(ContentHelper.prepare(parts[i]));
					sb.append("</td>");
					sb.append("<td></td>");
				}
				sb.append("</tr>");
			}
			room.updatedLineCount(parts.length);			
		} else {
			int line = room.lineCount + 1;
			sb.append("<tr data-line=\"" + line + "\" class=\"entry head \">");
			sb.append("<th class=\"tiny\">");
			sb.append("<a data-link=\"line-link\" id=\"L" + line + "\" name=\"L" + line + "\" href=\"#L" + line + "\">#" + line + "</a>");
			sb.append("</th>");
			sb.append("<td class=\"top narrow\">");
			sb.append(decorateIcon(entry.getSenderName()));
			sb.append("</td>");
			sb.append("<td>");
			sb.append("changed topic to <b>" + entry.subject + "</b>");
			sb.append("</td>");
			sb.append("<td class=\"top narrow\">" + TIME_FORMAT.format(entry.getDate()) + "</td>");
			sb.append("</tr>");			
			room.updatedLineCount(1);		
		}
		return sb.toString();
	}

	public static String decorateIcon(String senderName) {
		return addAvatarToUser(senderName, "64x64", false);
	}
	
	public static String decorateAvatar(String senderName) {
		return addAvatarToUser(senderName, "128x128", true);
	}
	
	private static String addAvatarToUser(String senderName, String size, boolean lineBreak){
		StringBuilder sb = new StringBuilder();
		if (USER_AVATAR_URL_TEMPLATE != null && User.exists(senderName)) {
			sb.append("<img class=\"avatar"+size+"\" src=\"" + USER_AVATAR_URL_TEMPLATE.replace(":uid", senderName).replace(":size", size) + "\"/>");
			if(USER_URL_TEMPLATE != null) {
				sb.append("<a href=\""+USER_URL_TEMPLATE.replace(":uid", senderName)).append("\">");
				if(lineBreak){
					sb.append("<br />");		
				}
				sb.append(senderName+"</a>");				
			} else {
				sb.append(senderName);
			}
		} else {
			sb.append(senderName);
		}
		return sb.toString();		
	}
}
