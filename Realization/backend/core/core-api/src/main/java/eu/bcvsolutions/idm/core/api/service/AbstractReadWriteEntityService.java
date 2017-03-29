package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Abstract implementation for generic CRUD operations on a repository for a
 * specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
 * @deprecated use {@link AbstractReadWriteDtoService}
 */
@Deprecated
public abstract class AbstractReadWriteEntityService<E extends BaseEntity, F extends BaseFilter>
		extends AbstractReadEntityService<E, F> implements ReadWriteEntityService<E, F> {
	
	public AbstractReadWriteEntityService(AbstractEntityRepository<E, F> repository) {
		super(repository);
	}

	/**
	 * Saves a given entity. Use the returned instance for further operations as
	 * the save operation might have changed the entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	@Override
	@Transactional
	public E save(E entity) {
		Assert.notNull(entity);
		//
		return getRepository().save(entity);
	}
	
	/**
	 * Saves all given entities.
	 * 
	 * @param entities
	 * @return the saved entities
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public Iterable<E> saveAll(Iterable<E> entities) {
		Assert.notNull(entities);
		//
		List<E> savedEntities = new ArrayList<>();
		entities.forEach(entity -> {
			savedEntities.add(save(entity));
		});		
		return savedEntities;
	}

	/**
	 * Deletes a given entity.
	 * 
	 * @param entity
	 * @throws IllegalArgumentException
	 *             in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void delete(E entity) {
		Assert.notNull(entity);
		//
		getRepository().delete(entity);
	}

	/**
	 * Deletes a given entity.
	 * 
	 * @param entity
	 * @throws IllegalArgumentException
	 *             in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void deleteById(Serializable id) {
		Assert.notNull(id);
		//
		delete(get(id));
	}

}
