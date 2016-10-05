package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @author Radek Tomiška 
 *
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity, F extends BaseFilter> extends PagingAndSortingRepository<E, Long> {

	@Override
	@Transactional(timeout = 10)
	Iterable<E> findAll();

	Page<E> find(F filter, Pageable pageable);
}
