package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;

/**
 * Filter for common eav forms
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 *
 */
public class IdmFormFilter extends DataFilter {

	private UUID ownerId;
	private String ownerType;
	private String ownerCode;
	
	public IdmFormFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmFormFilter(MultiValueMap<String, Object> data) {
		super(IdmFormDto.class, data);
	}

	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}
	
	public String getOwnerCode() {
		return ownerCode;
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