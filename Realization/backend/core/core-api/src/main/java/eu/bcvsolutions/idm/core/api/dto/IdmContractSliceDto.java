package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Contract time slice DTO
 *
 * @author Svanda
 */
@Relation(collectionRelation = "contractSlices")
public class IdmContractSliceDto extends IdmIdentityContractDto implements ValidableEntity {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID parentContract;
	@Size(max = DefaultFieldLengths.NAME)
	private String contractCode;
	private String code;

	public UUID getParentContract() {
		return parentContract;
	}

	public void setParentContract(UUID parentContract) {
		this.parentContract = parentContract;
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

}
