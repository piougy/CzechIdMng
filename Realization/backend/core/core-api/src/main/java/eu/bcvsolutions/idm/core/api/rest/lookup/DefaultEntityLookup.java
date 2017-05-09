package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.EntityManager;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Default entity lookup by {@code UUID} entityId
 * 
 * @param <E>
 * @author Radek Tomiška
 */
public class DefaultEntityLookup<E extends BaseEntity> implements EntityLookup<E> {

	private final EntityManager entityManager;
	private final Class<E> entityClass;

	public DefaultEntityLookup(EntityManager entityManager, Class<E> entityClass) {
		this.entityManager = entityManager;
		this.entityClass = entityClass;
	}

	@Override
	public Serializable getIdentifier(E entity) {
		return entity.getId();
	}

	@Override
	public E lookup(Serializable id) {
		if (AbstractEntity.class.isAssignableFrom(entityClass) && (id instanceof String)) {
			// workflow / rest usage with string uuid variant
			// EL does not recognize two methods with the same name and
			// different argument type
			try {
				return entityManager.find(entityClass, UUID.fromString((String) id));
			} catch (IllegalArgumentException ex) {
				// simply not found
				return null;
			}
		}
		return entityManager.find(entityClass, id);
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return BaseEntity.class.isAssignableFrom(delimiter);
	}

}
