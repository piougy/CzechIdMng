package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction for DTO services.
 * 
 * @author Svanda
 * @author Radek Tomiška
 * 
 * @see Sort
 * @see Pageable
 * @see Page
 * @param <DTO> {@link BaseDto} type
 * @param <F> {@link BaseFilter} type
 */
public interface ReadDtoService<DTO extends BaseDto, F extends BaseFilter>
		extends BaseDtoService<DTO> {

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this
	 * service
	 * 
	 * @return
	 */
	Class<F> getFilterClass();
	
	/**
	 * Returns DTO by given id. Returns null, if dto is not exists. Authorization policies are evaluated.
	 *
	 * @param id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	DTO get(Serializable id, BasePermission... permission);

	/**
	 * Returns page of DTOs
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<DTO> find(Pageable pageable, BasePermission... permission);
	
	/**
	 * Returns page of DTOs by given filter, authorization permission will be evaluated. 
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission permissions to evaluate
	 * @return
	 */
	Page<DTO> find(F filter, Pageable pageable, BasePermission... permission);
	
	/**
	 * Returns whether the given dto is considered to be new.
	 * 
	 * @param dto must never be {@literal null}
	 * @return
	 */
	boolean isNew(DTO dto);
	
	/**
	 * Returns, what currently logged identity can do with given dto / entity
	 * 
	 * @param backendId
	 * @return
	 */
	Set<String> getPermissions(Serializable id);
}
