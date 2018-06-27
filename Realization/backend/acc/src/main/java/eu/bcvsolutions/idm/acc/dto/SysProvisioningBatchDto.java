package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysProvisioningBatch}
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningBatchs")
public class SysProvisioningBatchDto extends AbstractDto {

	private static final long serialVersionUID = -6935661873072888426L;
	
	@NotNull
	@Embedded(dtoClass = SysSystemEntityDto.class)
	private UUID systemEntity; // account uid, etc.
	private DateTime nextAttempt;
	
	public SysProvisioningBatchDto() {
	}
	
	public SysProvisioningBatchDto(SysProvisioningOperationDto operation) {
		this.systemEntity = operation.getSystemEntity();
	}

	public DateTime getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(DateTime nextAttempt) {
		this.nextAttempt = nextAttempt;
	}

	public UUID getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(UUID systemEntity) {
		this.systemEntity = systemEntity;
	}
}
