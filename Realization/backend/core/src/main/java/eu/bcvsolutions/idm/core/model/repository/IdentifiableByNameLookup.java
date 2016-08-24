package eu.bcvsolutions.idm.core.model.repository;

import java.io.Serializable;

import org.springframework.data.rest.core.support.EntityLookupSupport;

import eu.bcvsolutions.idm.core.model.domain.IdentifiableByName;

/**
 * Some entities could be found by name
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 * @param <T>
 */
public abstract class IdentifiableByNameLookup<T extends IdentifiableByName> extends EntityLookupSupport<T>{
	
	public abstract T findOneByName(String name);
	public abstract T findOne(Long id);
	
	@Override
	public Serializable getResourceIdentifier(T entity) {
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
