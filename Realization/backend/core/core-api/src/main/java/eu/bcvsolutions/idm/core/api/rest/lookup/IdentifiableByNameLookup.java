package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;

/**
 * Some entities could be found by name.
 * 
 * @param <E>
 * @deprecated CodeableLookup will be added
 * @author Radek Tomiška
 */
@Deprecated
public abstract class IdentifiableByNameLookup<E extends BaseEntity> extends EntityLookupSupport<E>{
	
	protected abstract IdentifiableByNameEntityService<E> getEntityService();
	
	@Override
	public Serializable getResourceIdentifier(E entity) {
		if (entity instanceof IdentifiableByName) {
			return ((IdentifiableByName)entity).getName();
		} else {
			return entity.getId();
		}
	}

	@Override
	public Object lookupEntity(Serializable id) {
		E entity = null;
		try {
			entity = getEntityService().get(id);
		} catch (IllegalArgumentException ex) {
			// simply not found
		}
		if (entity == null) {
			entity = getEntityService().getByName(id.toString());
		}
		return entity;
	}
}
