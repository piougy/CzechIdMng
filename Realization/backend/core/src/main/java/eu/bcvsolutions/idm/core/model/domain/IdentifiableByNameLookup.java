package eu.bcvsolutions.idm.core.model.domain;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

/**
 * Some entities could be found by name
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 * @param <T>
 */
public abstract class IdentifiableByNameLookup<E extends IdentifiableByName> extends EntityLookupSupport<E>{
	
	public abstract E findOneByName(String name);
	public abstract E findOne(Long id);
	
	@Override
	public Serializable getResourceIdentifier(E entity) {
		return entity.getName();
	}

	@Override
	public Object lookupEntity(Serializable id) {
		IdentifiableByName entity = findOneByName(id.toString());
		if(entity == null) {
			try {
				entity = findOne(Long.valueOf(id.toString()));
			} catch (NumberFormatException ex) {
				// simply not found		
			}
		}
		return entity;
	}
}
