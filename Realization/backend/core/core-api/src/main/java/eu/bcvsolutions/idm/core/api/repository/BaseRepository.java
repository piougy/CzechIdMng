package eu.bcvsolutions.idm.core.api.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * TODO: QueryDslPredicateExecutor<E>
 * 
 * @author Radek Tomiška 
 *
 */
@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity, F extends BaseFilter> extends PagingAndSortingRepository<E, UUID> {

	@Override
	@Transactional(timeout = 10)
	Iterable<E> findAll();

	Page<E> find(F filter, Pageable pageable);
}
