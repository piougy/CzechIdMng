package eu.bcvsolutions.idm.core.api.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for abstract entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @author Radek Tomiška
 *
 * @param <E> entity type
 * @param <F> basic filter
 */
@NoRepositoryBean
public interface AbstractEntityRepository<E extends BaseEntity, F extends BaseFilter> extends BaseEntityRepository<E, UUID, F> {
	
	/**
	 * Quick filter - is need to be overridden in all sub interfaces.
	 * 
	 * @see {@link QuickFilter}
	 * @see {@link EmptyFilter}
	 * 
	 * @param filter
	 * @param pageable
	 * @deprecated use criteria api
	 * @return
	 */
	@Deprecated
	Page<E> find(F filter, Pageable pageable);
}
