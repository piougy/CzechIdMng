package eu.bcvsolutions.idm.acc.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.OperationResultDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningContextDto;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Provisioning operation "content" and state
 * 
 * @author Radek Tomiška
 *
 */
public interface ProvisioningOperation extends BaseEntity, Auditable {

	/**
	 * Provisioning operation type
	 * 
	 * @return
	 */
	ProvisioningEventType getOperationType();

	/**
	 * Target system id
	 * 
	 * @return
	 */
	UUID getSystem();

	/**
	 * IdM entity type
	 * 
	 * @return
	 */
	SystemEntityType getEntityType();

	/**
	 * IdM entity type identifier
	 * 
	 * @return
	 */
	UUID getEntityIdentifier();

	/**
	 * Target system entity identifier
	 * 
	 * @return
	 */
	String getSystemEntityUid();

	/**
	 * Result state
	 * 
	 * @return
	 */
	OperationState getResultState();

	/**
	 * Whole result with code and exception
	 * 
	 * @return
	 */
	OperationResultDto getResult();

	/**
	 * Provisioning "content"
	 * 
	 * @return
	 */
	ProvisioningContextDto getProvisioningContext();

}