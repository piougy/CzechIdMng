package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DelegationTypeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.eav.api.service.DelegationType;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import java.util.List;
import java.util.UUID;

/**
 * Delegation manager
 *
 * @author Vít Švanda
 * @since 10.4.0
 *
 */
public interface DelegationManager {

	String WORKFLOW_DELEGATION_TYPE_KEY = "delegationType";

	/**
	 * Finds delegation for given delegation type, delegator and owner (task).
	 *
	 * If no delegation for given delegation type will be found, the default
	 * delegation will be searching.
	 *
	 * @param type
	 * @param delegatorId
	 * @param delegatorContractId
	 * @param owner
	 *
	 * @return
	 */
	List<IdmDelegationDefinitionDto> findDelegation(String type, UUID delegatorId, UUID delegatorContractId, BaseDto owner);

	/**
	 * Delegate the given task (ownere) by given delegation definition.
	 * 
	 * @param owner
	 * @param definition
	 * @return 
	 */
	IdmDelegationDto delegate(BaseDto owner, IdmDelegationDefinitionDto definition);

	/**
	 * Returns all registred delegationTypes.
	 *
	 * @return
	 */
	List<DelegationType> getSupportedTypes();

	/**
	 * Converts delegationTyp to DTO version
	 *
	 * @param delegationType
	 *
	 * @return
	 */
	DelegationTypeDto convertDelegationTypeToDto(DelegationType delegationType);

	/**
	 * Find delegate type by bean name.
	 *
	 * @param id
	 *
	 * @return
	 */
	DelegationType getDelegateType(String id);

	/**
	 * Get delegation type form workflow process definition.
	 *
	 * @param wfDefinition Id of workflow process definition.
	 *
	 * @return If delegatio type is not defined in that process, then will be
	 *         returned null.
	 */
	String getProcessDelegationType(String wfDefinition);
	
	/**
	 * Find delegation for given owner.
	 * 
	 * @param owner
	 * @param permission
	 * @return
	 */
	List<IdmDelegationDto> findDelegationForOwner(BaseDto owner, BasePermission... permission);

}
