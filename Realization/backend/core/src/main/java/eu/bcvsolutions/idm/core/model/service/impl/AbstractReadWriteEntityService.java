package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Interface for generic CRUD operations on a repository for a specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public abstract class AbstractReadWriteEntityService<E extends BaseEntity> extends AbstractReadEntityService<E> implements ReadWriteEntityService<E> {

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	@Override
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
	public void delete(E entity) {
		getRepository().delete(entity);
	}

}
