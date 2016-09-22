package eu.bcvsolutions.idm.core.rest.domain;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

import eu.bcvsolutions.idm.core.model.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.IdentifiableByNameEntityService;

/**
 * Some entities could be found by name
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 * @param <T>
 */
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
		E entity = getEntityService().getByName(id.toString());
		if(entity == null) {
			try {
				entity = getEntityService().get(Long.valueOf(id.toString()));
			} catch (NumberFormatException ex) {
				// simply not found		
			}
		}
		return entity;
	}
}
