package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysProvisioningRequest}
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningRequests")
public class SysProvisioningRequestDto extends AbstractDto {

	private static final long serialVersionUID = 5873283928189346937L;
	
	private int currentAttempt = 0;
	private int maxAttempts;
	@Embedded(dtoClass = SysProvisioningOperationDto.class)
	private UUID operation;
	private OperationResultDto result;
	@Embedded(dtoClass = SysProvisioningBatchDto.class)
	private UUID batch;
	
	public SysProvisioningRequestDto() {
	}

	public SysProvisioningRequestDto(UUID operation) {
		this.operation = operation;
	}

	public int getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(int currentAttempt) {
		this.currentAttempt = currentAttempt;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public UUID getOperation() {
		return operation;
	}

	public void setOperation(UUID operation) {
		this.operation = operation;
	}

	public OperationResultDto getResult() {
		return result;
	}

	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	public UUID getBatch() {
		return batch;
	}

	public void setBatch(UUID batch) {
		this.batch = batch;
	}
	
	public void increaseAttempt() {
		this.currentAttempt++;
	}
}
