package eu.bcvsolutions.idm.core.rest;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Wrapper for deferred request and additional information
 *
 * @author Vít Švanda
 *
 */
public class LongPollingSubscriber {

	private UUID entityId;
	private ZonedDateTime lastTimeStamp;
	private Long lastNumberOfEntities;
	private Class<? extends AbstractDto> type;
	/**
	 * Time stamp when was this subscriber last used. It important for clearing map of subscribers.
	 */
	private ZonedDateTime lastUsingSubscriber;

	public LongPollingSubscriber(UUID entityId, Class<? extends AbstractDto> type) {
		this.entityId = entityId;
		this.type = type;
	}

	public ZonedDateTime getLastTimeStamp() {
		return lastTimeStamp;
	}

	public void setLastTimeStamp(ZonedDateTime lastTimeStamp) {
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

	public ZonedDateTime getLastUsingSubscriber() {
		return lastUsingSubscriber;
	}

	public void setLastUsingSubscriber(ZonedDateTime lastUsingSubscriber) {
		this.lastUsingSubscriber = lastUsingSubscriber;
	}

}