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
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

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
	 * @return saved state
	 */
	IdmEntityStateDto saveState(Identifiable owner, IdmEntityStateDto state);
	
	/**
	 * Create simple entity state. Use {@link #saveState(Identifiable, IdmEntityStateDto)}, if more state properties is needed.
	 * 
	 * @param owner state owner
	 * @param operationState [Optional] operation state, {@link OperationState#CREATED} will be used as default.
	 * @param code [Optional] code - additional info for operation state 
	 * @param code [Optional] properties additional result model properties
	 * @return created entity state
	 * @see #saveState(Identifiable, IdmEntityStateDto)
	 * @since 10.4.0
	 */
	IdmEntityStateDto createState(
			Identifiable owner, 
			OperationState operationState, 
			ResultCode code,
			Map<String, Serializable> properties);
	
	/**
	 * Find states for given owner.
	 * 
	 * @param owner
	 * @param pageable
	 * @return
	 */
	Page<IdmEntityStateDto> findStates(Identifiable owner, Pageable pageable);
	
	/**
	 * Get states by given filter.
	 * 
	 * @see IdmEntityStateService
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<IdmEntityStateDto> findStates(IdmEntityStateFilter filter, Pageable pageable);
	
	/**
	 * Delete entity state.
	 * 
	 * @param state
	 */
	void deleteState(IdmEntityStateDto state);
}
