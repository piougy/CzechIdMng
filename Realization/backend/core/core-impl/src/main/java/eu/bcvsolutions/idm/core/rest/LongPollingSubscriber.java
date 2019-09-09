package eu.bcvsolutions.idm.core.rest;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;

/**
 * Wrapper for deferred request and additional information
 * 
 * @author Vít Švanda
 *
 */
public class LongPollingSubscriber{

	private UUID entityId;
	private DateTime lastTimeStamp;
	private OperationResultDto lastOperationResult;
	private Long lastNumberOfEntities;
	private Class<? extends AbstractDto> type;
	/**
	 * Time stamp when was this subscriber last used. It important for clearing map of subscribers.
	 */
	private DateTime lastUsingSubscriber;
	
	public LongPollingSubscriber() {
		super();
	}

	public LongPollingSubscriber(UUID entityId, Class<? extends AbstractDto> type) {
		this.entityId = entityId;
		this.type = type;
	}
	

	public DateTime getLastTimeStamp() {
		return lastTimeStamp;
	}

	public void setLastTimeStamp(DateTime lastTimeStamp) {
		this.lastTimeStamp = lastTimeStamp;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public OperationResultDto getLastOperationResult() {
		return lastOperationResult;
	}

	public void setLastOperationResult(OperationResultDto lastOperationResult) {
		this.lastOperationResult = lastOperationResult;
	}

	public Long getLastNumberOfEntities() {
		return lastNumberOfEntities;
	}

	public void setLastNumberOfEntities(Long lastNumberOfEntities) {
		this.lastNumberOfEntities = lastNumberOfEntities;
	}

	public Class<? extends AbstractDto> getType() {
		return type;
	}

	public void setType(Class<? extends AbstractDto> type) {
		this.type = type;
	}

	public DateTime getLastUsingSubscriber() {
		return lastUsingSubscriber;
	}

	public void setLastUsingSubscriber(DateTime lastUsingSubscriber) {
		this.lastUsingSubscriber = lastUsingSubscriber;
	}

	@Override
	public String toString() {
		return String.format(
				"LongPollingSubscriber [entityId=%s, lastTimeStamp=%s, lastOperationResult=%s, lastNumberOfEntities=%s, type=%s, lastUsingSubscriber=%s]",
				entityId, lastTimeStamp, lastOperationResult, lastNumberOfEntities, type, lastUsingSubscriber);
	}

}