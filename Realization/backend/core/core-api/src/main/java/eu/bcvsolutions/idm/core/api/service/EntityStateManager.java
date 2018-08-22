package eu.bcvsolutions.idm.core.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;

/**
 * Entity states
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface EntityStateManager {
	
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
	 * Saves state to given owner
	 * 
	 * @param owner
	 * @param state Owner props will be filled automaticaly
	 * @return
	 */
	IdmEntityStateDto saveState(Identifiable owner, IdmEntityStateDto state);
	
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
	 * Delete entity state
	 * 
	 * @param state
	 */
	void deleteState(IdmEntityStateDto state);
}
