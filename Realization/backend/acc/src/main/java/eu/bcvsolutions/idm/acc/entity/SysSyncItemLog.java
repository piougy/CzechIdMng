package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSyncItemLog</i> is responsible for keep log informations about specific
 * synchronized item.
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_item_log")
public class SysSyncItemLog extends AbstractEntity {

	private static final long serialVersionUID = -5447620157233410338L;
	private static final String LOG_SEPARATOR = "-------------------------";

	@NotNull
	@ManyToOne(optional = false, cascade = { CascadeType.ALL })
	@JoinColumn(name = "sync_action_log_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSyncActionLog syncActionLog;

	@Size(max = DefaultFieldLengths.NAME)
	@NotNull
	@Column(name = "identification", length = DefaultFieldLengths.NAME)
	private String identification;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "display_name", length = DefaultFieldLengths.NAME, nullable = true)
	private String displayName;

	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "message", length = DefaultFieldLengths.LOG, nullable = true)
	private String message;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "type", length = DefaultFieldLengths.NAME, nullable = true)
	private String type;

	@Lob
	@Column(name = "log")
	private String log;

	public SysSyncItemLog() {
		super();
	}

	public SysSyncItemLog(SysSyncActionLog syncActionLog, String identification, String displayName, String message,
			String log) {
		super();
		this.syncActionLog = syncActionLog;
		this.identification = identification;
		this.displayName = displayName;
		this.message = message;
		this.log = log;
	}

	public SysSyncActionLog getSyncActionLog() {
		return syncActionLog;
	}

	public void setSyncActionLog(SysSyncActionLog syncActionLog) {
		this.syncActionLog = syncActionLog;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String addToLog(String text) {
		if (text != null) {
			StringBuilder builder = new StringBuilder();
			if (this.log != null) {
				builder.append(this.log);
				builder.append("\n" + LOG_SEPARATOR + "\n");
			}
			builder.append(text);
			this.setLog(builder.toString());
			if (!(text.length() > DefaultFieldLengths.NAME)) {
				this.setMessage(text);
			}
		}
		return this.getLog();
	}
}
