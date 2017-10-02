package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.EntityManager;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Default entity lookup by {@code UUID} entityId.
 * Can be used for lookup {@link Codeable} entities too - dtoLookup has to be given
 * 
 * @param <E>
 * @author Radek Tomiška
 */
public class DefaultEntityLookup<E extends BaseEntity> implements EntityLookup<E> {

	private final EntityManager entityManager;
	private final Class<E> entityClass;
	private final DtoLookup<?> dtoLookup;
	
	public DefaultEntityLookup(EntityManager entityManager, Class<E> entityClass) {
		this(entityManager, entityClass, null);
	}

	public DefaultEntityLookup(EntityManager entityManager, Class<E> entityClass, DtoLookup<?> dtoLookup) {
		this.entityManager = entityManager;
		this.entityClass = entityClass;
		this.dtoLookup = dtoLookup;
	}

	@Override
	public Serializable getIdentifier(E entity) {
		return entity.getId();
	}

	@Override
	public E lookup(Serializable id) {
		// by dto lookup
		if (dtoLookup != null 
				&& (id instanceof String)
				&& Codeable.class.isAssignableFrom(entityClass)) {
			BaseDto dto = dtoLookup.lookup(id);
			if (dto == null) {
				return null;
			}
			return find(dto.getId());
		}
		// default by id
		if (AbstractEntity.class.isAssignableFrom(entityClass) && (id instanceof String)) {
			// workflow / rest usage with string uuid variant
			// EL does not recognize two methods with the same name and
			// different argument type
			try {
				return find(UUID.fromString((String) id));
			} catch (IllegalArgumentException ex) {
				// simply not found
				return null;
			}
		}
		return find(id);
	}
	
	private E find(Serializable id) {
		return entityManager.find(entityClass, id);
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return BaseEntity.class.isAssignableFrom(delimiter);
	}

}
