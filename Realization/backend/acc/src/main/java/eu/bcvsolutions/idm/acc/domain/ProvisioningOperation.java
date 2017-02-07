package eu.bcvsolutions.idm.acc.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

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
	 * Target system
	 * 
	 * @return
	 */
	SysSystem getSystem();

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
	OperationResult getResult();

	/**
	 * Provisioning "content"
	 * 
	 * @return
	 */
	ProvisioningContext getProvisioningContext();

}