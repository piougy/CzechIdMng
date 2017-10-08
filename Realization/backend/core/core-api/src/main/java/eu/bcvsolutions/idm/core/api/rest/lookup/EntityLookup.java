package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Read {@link BaseEntity} by uuid identifier or by {@link Codeable} identifier.
 * 
 * @author Radek Tomiška
 *
 * @param <T> {@link BaseEntity} type
 */
public interface EntityLookup<T extends BaseEntity> extends Plugin<Class<?>> {

	/**
	 * Gets {@link BaseEntity} identifier - {@link Codeable} identifier has higher priority.
	 * 
	 * @param entity
	 * @return
	 */
	Serializable getIdentifier(T entity);

	/**
	 * Returns {@link BaseEntity} by given identifier.
	 * If permissions are given, then will be evaluated.
	 * 
	 * @param id
	 * @return {@link BaseEntity}
	 */
	T lookup(Serializable id);
}
