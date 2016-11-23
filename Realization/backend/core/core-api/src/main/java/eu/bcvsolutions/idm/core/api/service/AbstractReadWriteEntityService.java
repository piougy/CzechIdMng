package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Abstract implementation for generic CRUD operations on a repository for a
 * specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
 */
public abstract class AbstractReadWriteEntityService<E extends BaseEntity, F extends BaseFilter>
		extends AbstractReadEntityService<E, F> implements ReadWriteEntityService<E, F> {

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
		return getRepository().save(entity);
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
