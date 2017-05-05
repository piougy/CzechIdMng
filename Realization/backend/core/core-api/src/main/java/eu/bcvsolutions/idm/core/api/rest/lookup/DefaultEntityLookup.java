package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Default entity lookup by {@code Long} entityId
 * 
 * @param <E>
 * @author Radek Tomiška
 */
public class DefaultEntityLookup<E extends BaseEntity> extends EntityLookupSupport<E> {

	private final ReadEntityService<E, ?> service;

	public DefaultEntityLookup(ReadEntityService<E, ?> service) {
		this.service = service;
	}

	@Override
	public Serializable getResourceIdentifier(E entity) {
		return entity.getId();
	}

	@Override
	public Object lookupEntity(Serializable id) {
		return service.get(id);
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return service.getEntityClass().isAssignableFrom(delimiter);
	}

}
