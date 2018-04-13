package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;

/**
 * Filter for {@link IdmContractSliceDto} dtos.
 *
 * @author svandav
 */
public class IdmContractSliceFilter extends IdmIdentityContractFilter implements CorrelationFilter{
	
	private UUID excludeContract; // For choose the parent contract. I want to exclude itself contract.
	private Boolean withoutParent; // Returns contract without filled the parent field.
	private UUID parentContract; // Internal relation on the main identity-contract
	private String contractCode; // Identifier for the main contract.
	private String code; // Identifier of the slice
	
	public IdmContractSliceFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmContractSliceFilter(MultiValueMap<String, Object> data) {
		super(IdmContractSliceDto.class, data);
	}
	
	public UUID getExcludeContract() {
		return excludeContract;
	}

	public void setExcludeContract(UUID excludeContract) {
		this.excludeContract = excludeContract;
	}

	public Boolean getWithoutParent() {
		return withoutParent;
	}

	public void setWithoutParent(Boolean withoutParent) {
		this.withoutParent = withoutParent;
	}

	public String getContractCode() {
		return contractCode;
	}

	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public UUID getParentContract() {
		return parentContract;
	}

	public void setParentContract(UUID parentContract) {
		this.parentContract = parentContract;
	}
	
}
