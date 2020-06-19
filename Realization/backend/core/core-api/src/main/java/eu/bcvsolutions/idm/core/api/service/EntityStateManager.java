package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Entity states.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface EntityStateManager extends ScriptEnabled {
	
	/**
	 * Owner type has to be entity class - dto instance can be given.
	 * 
	 * @param owner
	 * @return
	 */
	String getOwnerType(Identifiable owner);
	
	/**
	 * Owner type has to be entity class - dto class can be given.
	 * 
	 * @param ownerType
	 * @return
	 */
	String getOwnerType(Class<? extends Identifiable> ownerType);
	
	/**
	 * Saves state to given owner.
	 * 
	 * @param owner [optional] state owner, fill state owner direclty, if owner instance is not available (fill state ownerId and ownerType)
	 * @param state [required] Owner props will be filled automaticaly
	 * @param permission base permissions to evaluate (AND) 
	 * @return saved state
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmEntityStateDto saveState(Identifiable owner, IdmEntityStateDto state, BasePermission... permission);
	
	/**
	 * Create simple entity state. Use {@link #saveState(Identifiable, IdmEntityStateDto)}, if more state properties is needed.
	 * 
	 * @param owner state owner
	 * @param operationState [Optional] operation state, {@link OperationState#CREATED} will be used as default.
	 * @param code [Optional] code - additional info for operation state 
	 * @param code [Optional] properties additional result model properties
	 * @param permission base permissions to evaluate (AND) 
	 * @return created entity state
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @see #saveState(Identifiable, IdmEntityStateDto)
	 * @since 10.4.0
	 */
	IdmEntityStateDto createState(
			Identifiable owner, 
			OperationState operationState, 
			ResultCode code,
			Map<String, Serializable> properties,
			BasePermission... permission);
	
	/**
	 * Find states for given owner.
	 * 
	 * @param owner
	 * @param pageable
	 * @param permission base permissions to evaluate (AND) 
	 * @return states
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	Page<IdmEntityStateDto> findStates(Identifiable owner, Pageable pageable, BasePermission... permission);
	
	/**
	 * Get states by given filter.
	 * 
	 * @see IdmEntityStateService
	 * @param filter
	 * @param pageable
	 * @param permission base permissions to evaluate (AND) 
	 * @return states
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	Page<IdmEntityStateDto> findStates(IdmEntityStateFilter filter, Pageable pageable, BasePermission... permission);
	
	/**
	 * Delete entity state.
	 * 
	 * @param state
	 * @param permission base permissions to evaluate (AND) 
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void deleteState(IdmEntityStateDto state, BasePermission... permission);
	
	/**
	 * Delete owner states. Additional operation state and result code can be given - states with given code and state will be deleted.
	 * 
	 * @param owner [required] state owner
	 * @param operationState [Optional] operation state - all will be deleted by default
	 * @param code [Optional] code - all will be deleted by default
	 * @param permission base permissions to evaluate (AND) 
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 10.4.0
	 */
	void deleteStates(Identifiable owner, OperationState operationState, ResultCode code, BasePermission... permission);
}
