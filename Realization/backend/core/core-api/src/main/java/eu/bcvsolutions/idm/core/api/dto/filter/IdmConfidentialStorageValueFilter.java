package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;

/**
 * Filter for confidential storage value
 * 
 * @author Patrik Stloukal
 */
public class IdmConfidentialStorageValueFilter extends DataFilter {

	private UUID ownerId;
	private String ownerType;
	private String key;
	
	public IdmConfidentialStorageValueFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmConfidentialStorageValueFilter(MultiValueMap<String, Object> data) {
		super(IdmConfidentialStorageValueDto.class, data);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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
}