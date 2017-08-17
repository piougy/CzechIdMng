package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Dto for {@link SysSyncItemLog}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "syncItemLogs")
public class SysSyncItemLogDto extends AbstractDto implements Loggable {

	private static final long serialVersionUID = -3654357107748053073L;

	private SysSyncActionLog syncActionLog;
	private String identification;
	private String displayName;
	private String message;
	private String type;
	private String log;

	public SysSyncItemLogDto() {
		super();
	}
	
	public SysSyncItemLogDto(SysSyncActionLog syncActionLog, String identification, String displayName, String message,
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
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
