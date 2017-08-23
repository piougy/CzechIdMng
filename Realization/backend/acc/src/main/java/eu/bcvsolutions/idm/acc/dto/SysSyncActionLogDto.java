package eu.bcvsolutions.idm.acc.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO fot {@link SysSyncActionLog}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "syncActionLogs")
public class SysSyncActionLogDto extends AbstractDto {

	private static final long serialVersionUID = 574265554132998339L;
	
	@Embedded(dtoClass = SysSyncLogDto.class)
	private UUID syncLog;
	private SynchronizationActionType syncAction;
	private Integer operationCount = 0;
	private OperationResultType operationResult;
	@JsonIgnore
	private List<SysSyncItemLogDto> logItems;
	
	public UUID getSyncLog() {
		return syncLog;
	}

	public void setSyncLog(UUID syncLog) {
		this.syncLog = syncLog;
	}

	public SynchronizationActionType getSyncAction() {
		return syncAction;
	}

	public void setSyncAction(SynchronizationActionType syncAction) {
		this.syncAction = syncAction;
	}

	public Integer getOperationCount() {
		return operationCount;
	}

	public void setOperationCount(Integer operationCount) {
		this.operationCount = operationCount;
	}

	public OperationResultType getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResultType operationResult) {
		this.operationResult = operationResult;
	}

	public List<SysSyncItemLogDto> getLogItems() {
		if (logItems == null) {
			return new ArrayList<>();
		}
		return logItems;
	}

	public void setLogItems(List<SysSyncItemLogDto> logItems) {
		this.logItems = logItems;
	}

	/**
	 * Method add to list of {@link SysSyncItemLogDto} new logItem, before add
	 * check if list is not null (create) and check if list not contains this
	 * log.
	 * 
	 * @param logItem
	 */
	public void addLogItems(SysSyncItemLogDto logItem) {
		if (logItems == null) {
			logItems = new ArrayList<>();
		}
		// try found same log in list, not duplicate
		Optional<SysSyncItemLogDto> foundLog = logItems.stream().filter(log -> log.equals(logItem)).findFirst();
		if (foundLog.isPresent()) {
			logItems.remove(foundLog.get());
		}
		logItems.add(logItem);
	}
}
