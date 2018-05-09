package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;

/**
 * Filter for identity contract's guarantees
 * 
 * @author Radek Tomiška
 *
 */
public class IdmContractGuaranteeFilter extends DataFilter implements ExternalIdentifiable {

	private UUID identityContractId;
	private UUID guaranteeId;
	
	public IdmContractGuaranteeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmContractGuaranteeFilter(MultiValueMap<String, Object> data) {
		super(IdmContractGuaranteeDto.class, data);
	}

	public UUID getIdentityContractId() {
		return identityContractId;
	}
	
	public void setIdentityContractId(UUID identityContractId) {
		this.identityContractId = identityContractId;
	}
	
	public UUID getGuaranteeId() {
		return guaranteeId;
	}
	
	public void setGuaranteeId(UUID guaranteeId) {
		this.guaranteeId = guaranteeId;
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
}
