package eu.bcvsolutions.idm.core.api.dto.filter;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.LogType;

/**
 * Filter for logging event
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class LoggingEventFilter extends QuickFilter {

	private Long eventId;
	private DateTime from;
	private DateTime till;
	private String loggerName;
	private LogType levelString;
	private String callerFilename;
	private String callerClass;
	private String callerMethod;
	private String callerLine;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public DateTime getFrom() {
		return from;
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public DateTime getTill() {
		return till;
	}

	public void setTill(DateTime till) {
		this.till = till;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public LogType getLevelString() {
		return levelString;
	}

	public void setLevelString(LogType levelString) {
		this.levelString = levelString;
	}

	public String getCallerFilename() {
		return callerFilename;
	}

	public void setCallerFilename(String callerFilename) {
		this.callerFilename = callerFilename;
	}

	public String getCallerClass() {
		return callerClass;
	}

	public void setCallerClass(String callerClass) {
		this.callerClass = callerClass;
	}

	public String getCallerMethod() {
		return callerMethod;
	}

	public void setCallerMethod(String callerMethod) {
		this.callerMethod = callerMethod;
	}

	public String getCallerLine() {
		return callerLine;
	}

	public void setCallerLine(String callerLine) {
		this.callerLine = callerLine;
	}
}
