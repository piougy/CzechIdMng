package eu.bcvsolutions.idm.core.audit.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.LogType;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity for logging event. Entity is only for read.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "logging_event", indexes = { 
		})
public class IdmLoggingEvent implements BaseEntity {

	private static final long serialVersionUID = 5803765065103354200L;

	@Id
	@Column(name = "event_id", nullable = true)
	private Long eventId;

	@Column(name = "timestmp", nullable = false)
	private Long timestmp;
	
	@Column(name = "formatted_message", nullable = false)
	@Type(type = "org.hibernate.type.StringClobType")
	private String formattedMessage;
	
	@Column(name = "logger_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String loggerName;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "level_string", nullable = false)
	private LogType levelString;
	
	@Column(name = "thread_name", length = DefaultFieldLengths.NAME)
	private String threadName;
	
	@Column(name = "reference_flag")
	private Integer referenceFlag;
	
	@Column(name = "arg0")
	private String arg0;
	
	@Column(name = "arg1")
	private String arg1;
	
	@Column(name = "arg2")
	private String arg2;
	
	@Column(name = "arg3")
	private String arg3;
	
	@Column(name = "caller_filename", length = DefaultFieldLengths.NAME, nullable = false)
	private String callerFilename;
	
	@Column(name = "caller_class", length = DefaultFieldLengths.NAME, nullable = false)
	private String callerClass;
	
	@Column(name = "caller_method", length = DefaultFieldLengths.NAME, nullable = false)
	private String callerMethod;
	
	@Column(name = "caller_line", length = DefaultFieldLengths.NAME, nullable = false)
	private String callerLine;

	@Override
	public Serializable getId() {
		return this.eventId;
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
