package eu.bcvsolutions.idm.core.audit.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.audit.entity.key.IdmLoggingEventPropertyPrimaryKey;

/**
 * Entity logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "logging_event_property")
@IdClass(IdmLoggingEventPropertyPrimaryKey.class)
public class IdmLoggingEventProperty implements BaseEntity {

	private static final long serialVersionUID = 6574958320726121293L;

	@Id
	private Long eventId;
	
	@Id
	private String mappedKey;
	
	@Column(name = "mapped_value", length = 1024)
	private String mappedValue;
	
	@Override
	public Serializable getId() {
		return new IdmLoggingEventPropertyPrimaryKey(eventId, mappedKey);
	}

	@Override
	public void setId(Serializable id) {
		Assert.notNull(id);
		if (id instanceof IdmLoggingEventPropertyPrimaryKey) {
			IdmLoggingEventPropertyPrimaryKey primaryKey = ((IdmLoggingEventPropertyPrimaryKey)id);
			this.eventId = primaryKey.getEventId();
			this.mappedKey = primaryKey.getMappedKey();
		} else {
			throw new IllegalArgumentException("ID isnot instaceof IdmLoggingEventPropertyPrimaryKey, Instance: " + id.getClass());
		}
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getMappedKey() {
		return mappedKey;
	}

	public void setMappedKey(String mappedKey) {
		this.mappedKey = mappedKey;
	}

	public String getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(String mappedValue) {
		this.mappedValue = mappedValue;
	}	
}
