package eu.bcvsolutions.idm.core.api.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * @author Radek Tomiška
 *
 * @param <E> entity type
 * @param <ID> entity identifier type
 * @param <F> basic filter
 */
@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity, ID extends Serializable, F extends BaseFilter> extends PagingAndSortingRepository<E, ID> {

	/**
	 * Find all is not supposed to be used on big recourd counts
	 */
	@Override
	@Transactional(timeout = 10, readOnly = true)
	Iterable<E> findAll();

	/**
	 * Quick filter
	 * 
	 * @see {@link QuickFilter}
	 * @see {@link EmptyFilter}
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Page<E> find(F filter, Pageable pageable);
}

