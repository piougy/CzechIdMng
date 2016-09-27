package eu.bcvsolutions.idm.core.model.service;

import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

/**
 * All entity services using this interface.
 * 
 * @author Radek Tomiška
 *
 * @param <T> {@link BaseEntity} type
 */
public interface BaseEntityService<E extends BaseEntity> {

	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this service
	 * 
	 * @return
	 */
	public Class<E> getEntityClass();
}
