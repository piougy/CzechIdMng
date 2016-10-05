package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Interface for generic CRUD operations on a repository for a specific type.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity} type
 */
public interface ReadWriteEntityService<E extends BaseEntity, F extends BaseFilter> extends ReadEntityService<E, F> {

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	E save(E entity);
	
	/**
	 * Deletes a given entity.
	 * 
	 * @param entity
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	void delete(E entity);
}
