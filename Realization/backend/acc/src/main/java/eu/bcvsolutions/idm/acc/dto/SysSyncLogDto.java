package eu.bcvsolutions.idm.acc.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysSyncLog}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "syncItemLogs")
public class SysSyncLogDto extends AbstractDto implements Loggable {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SysSyncLogDto.class);
	private static final long serialVersionUID = -4364209149375365217L;
	
	@Embedded(dtoClass = SysSyncConfigDto.class)
	private UUID synchronizationConfig;
	private boolean running = false;
	private boolean containsError = false;
	private LocalDateTime started;
	private LocalDateTime ended;
	private String token;
	private String log;
	private List<SysSyncActionLogDto> syncActionLogs;

	public UUID getSynchronizationConfig() {
		return synchronizationConfig;
	}

	public void setSynchronizationConfig(UUID synchronizationConfig) {
		this.synchronizationConfig = synchronizationConfig;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isContainsError() {
		return containsError;
	}

	public void setContainsError(boolean containsError) {
		this.containsError = containsError;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setStarted(LocalDateTime started) {
		this.started = started;
	}

	public LocalDateTime getEnded() {
		return ended;
	}

	public void setEnded(LocalDateTime ended) {
		this.ended = ended;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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
			LOG.info(text);
			StringBuilder builder = new StringBuilder();
			if (this.log != null) {
				builder.append(this.log);
				builder.append("\n" + SysSyncItemLog.LOG_SEPARATOR + "\n");
			}
			builder.append(text);
			this.setLog(builder.toString());
		}
		return this.getLog();
	}

	@JsonIgnore
	public List<SysSyncActionLogDto> getSyncActionLogs() {
		if (this.syncActionLogs == null) {
			this.syncActionLogs = new ArrayList<>();
		}
		return syncActionLogs;
	}

	public void setSyncActionLogs(List<SysSyncActionLogDto> syncActionLogs) {
		this.syncActionLogs = syncActionLogs;
	}
}
