package eu.bcvsolutions.idm.core.ecm.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;

/**
 * Filter for attachments
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 *
 */
public class IdmAttachmentFilter extends DataFilter {

	private UUID ownerId;
	private String ownerType;
	private String name;
	private Boolean lastVersionOnly; // true - last version only
	private UUID versionsFor; // attachment id - all versions for attachment
	
	public IdmAttachmentFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAttachmentFilter(MultiValueMap<String, Object> data) {
		super(IdmAttachmentDto.class, data);
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLastVersionOnly(Boolean lastVersionOnly) {
		this.lastVersionOnly = lastVersionOnly;
	}
	
	public Boolean getLastVersionOnly() {
		return lastVersionOnly;
	}
	
	public UUID getVersionsFor() {
		return versionsFor;
	}
	
	public void setVersionsFor(UUID versionsFor) {
		this.versionsFor = versionsFor;
	}
}