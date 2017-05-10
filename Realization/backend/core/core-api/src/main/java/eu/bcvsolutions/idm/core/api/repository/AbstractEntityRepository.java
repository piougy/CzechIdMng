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
 * <p>
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @param <E> entity type
 * @param <F> basic filter
 * @author Radek Tomiška
 */
@NoRepositoryBean
public interface AbstractEntityRepository<E extends BaseEntity, F extends BaseFilter> extends BaseEntityRepository<E, UUID, F> {
	
	/**
	 * Quick filter - is need to be overridden in all sub interfaces.
	 * 
	 * @param filter
	 * @param pageable
	 * @return
     * @see {@link QuickFilter}
     * @see {@link EmptyFilter}
	 */
	Page<E> find(F filter, Pageable pageable);

}
