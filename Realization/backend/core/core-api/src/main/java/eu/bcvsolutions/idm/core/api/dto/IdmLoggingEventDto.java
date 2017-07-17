package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.LogType;

/**
 * Default DTO for IdmLoggingEvent.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "loggingEvents")
public class IdmLoggingEventDto implements BaseDto {
	
	private static final long serialVersionUID = 6857347948825028141L;

	private Long eventId;

	private Long timestmp;
	
	private String formattedMessage;
	
	private String loggerName;
	
	private LogType levelString;
	
	private String threadName;
	
	private Integer referenceFlag;
	
	private String arg0;
	
	private String arg1;
	
	private String arg2;
	
	private String arg3;
	
	private String callerFilename;
	
	private String callerClass;
	
	private String callerMethod;
	
	private String callerLine;

	@Override
	public Serializable getId() {
		return eventId;
	}

	@Override
	public void setId(Serializable id) {
		this.eventId = (Long) id;
	}

	public Long getTimestmp() {
		return timestmp;
	}

	public void setTimestmp(Long timestmp) {
		this.timestmp = timestmp;
	}

	public String getFormattedMessage() {
		return formattedMessage;
	}

	public void setFormattedMessage(String formattedMessage) {
		this.formattedMessage = formattedMessage;
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

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public Integer getReferenceFlag() {
		return referenceFlag;
	}

	public void setReferenceFlag(Integer referenceFlag) {
		this.referenceFlag = referenceFlag;
	}

	public String getArg0() {
		return arg0;
	}

	public void setArg0(String arg0) {
		this.arg0 = arg0;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public String getArg3() {
		return arg3;
	}

	public void setArg3(String arg3) {
		this.arg3 = arg3;
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
