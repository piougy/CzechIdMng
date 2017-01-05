package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 * 
 * @author Radek Tomiška
 * @see Sort
 * @see Pageable
 * @see Page
 */
public interface ReadEntityService<E extends BaseEntity, F extends BaseFilter> extends BaseEntityService<E> {

	/**
	 * Returns {@link BaseFilter} type class, which is controlled by this service
	 * 
	 * @return
	 */
	Class<F> getFilterClass();
	
	/**
	 * Returns entity by given id. Returns null, if entity is not exists.
	 * 
	 * @param id
	 * @return
	 */
	E get(Serializable id);
	
	/**
	 * Returns page of entities
	 * 
	 * @param pageable
	 * @return
	 */
	Page<E> find(Pageable pageable);
	
	/**
	 * Returns page of entities by given filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);
}
