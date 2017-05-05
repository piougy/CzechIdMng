package eu.bcvsolutions.idm.acc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Target system setting - is used for accont management and provisioning DTO
 * 
 * @author Svanda
 *
 */
public class SysSystemDto extends AbstractDto implements Codeable, FormableEntity, Disableable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private boolean readonly;
	private boolean disabled;
	private boolean queue;
	private boolean virtual;
	private Long version; // Optimistic lock - will be used with ETag
	private boolean remote;

	public String getName() {
		return name;
	}
	
	@Override
	@JsonIgnore
	public String getCode() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isQueue() {
		return queue;
	}

	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

}
