package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.joda.time.LocalDate;
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
	private String contractCode; // Identifier of the main contract on the source system
	private boolean usingAsContract; // Is this slice actually using as the contract?
	private LocalDate contractValidFrom;
	private LocalDate contractValidTill;

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

	public LocalDate getContractValidFrom() {
		return contractValidFrom;
	}

	public void setContractValidFrom(LocalDate contractValidFrom) {
		this.contractValidFrom = contractValidFrom;
	}

	public LocalDate getContractValidTill() {
		return contractValidTill;
	}

	public void setContractValidTill(LocalDate contractValidTill) {
		this.contractValidTill = contractValidTill;
	}

	public boolean isUsingAsContract() {
		return usingAsContract;
	}

	public void setUsingAsContract(boolean usingAsContract) {
		this.usingAsContract = usingAsContract;
	}

}
