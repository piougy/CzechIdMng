package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Interface for generic CRUD operations on a repository for a specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public abstract class AbstractReadWriteEntityService<E extends BaseEntity, F extends BaseFilter> extends AbstractReadEntityService<E, F> implements ReadWriteEntityService<E, F> {

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
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
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void delete(E entity) {
		getRepository().delete(entity);
	}

}
