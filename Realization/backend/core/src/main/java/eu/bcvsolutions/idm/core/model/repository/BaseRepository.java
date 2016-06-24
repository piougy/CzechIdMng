package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

/**
 * Common repository for base entities
 * 
 * TODO: QueryDslPredicateExecutor<T>
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends PagingAndSortingRepository<T, Long> {

	@Override
	@Transactional(timeout = 10)
	public Iterable<T> findAll();
}
