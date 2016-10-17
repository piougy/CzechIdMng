package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Default entity lookup by {@code Long} entityId
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public class DefaultEntityLookup<E extends BaseEntity> extends EntityLookupSupport<E> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultEntityLookup.class);

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
		try {
			return service.get(Long.valueOf(id.toString()));
		} catch (NumberFormatException ex) {
			log.warn("Wrong entity id [{}], expecting Long", id);
			return null;
		}
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return service.getEntityClass().isAssignableFrom(delimiter);
	}

}
