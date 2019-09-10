package eu.bcvsolutions.idm.core.rest;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Wrapper for deferred request and additional information
 * 
 * @author Vít Švanda
 *
 */
public class LongPollingSubscriber{

	private UUID entityId;
	private DateTime lastTimeStamp;
	private Long lastNumberOfEntities;
	private Class<? extends AbstractDto> type;
	/**
	 * Time stamp when was this subscriber last used. It important for clearing map of subscribers.
	 */
	private DateTime lastUsingSubscriber;

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

	public Long getLastNumberOfEntities() {
		return lastNumberOfEntities;
	}

	public void setLastNumberOfEntities(Long lastNumberOfEntities) {
		this.lastNumberOfEntities = lastNumberOfEntities;
	}

	public Class<? extends AbstractDto> getType() {
		return type;
	}
	
	public DateTime getLastUsingSubscriber() {
		return lastUsingSubscriber;
	}

	public void setLastUsingSubscriber(DateTime lastUsingSubscriber) {
		this.lastUsingSubscriber = lastUsingSubscriber;
	}

}