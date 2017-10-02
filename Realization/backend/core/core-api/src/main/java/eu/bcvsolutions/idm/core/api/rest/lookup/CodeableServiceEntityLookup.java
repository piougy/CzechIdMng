package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.CodeableService;

/**
 * Idebtifiable by code (codeable) lookup
 * 
 * @param <E>
 * @author Radek Tomiška
 * @deprecated use {@link CodeableDtoLookup}
 */
@Deprecated
public class CodeableServiceEntityLookup<E extends BaseEntity> extends AbstractEntityLookup<E>{
	
	private CodeableService<E> service;
	
	public CodeableServiceEntityLookup() {
	}
	
	public CodeableServiceEntityLookup(CodeableService<E> service) {
		this.service = service;
	}
	
	protected CodeableService<E> getService() {
		return service;
	}
	
	protected void setService(CodeableService<E> service) {
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public Serializable getIdentifier(E codeable) {
		if (codeable instanceof Codeable) {
			return ((Codeable) codeable).getCode();
		} else {
			return codeable.getId();
		}
	}

	@Override
	public E lookup(Serializable id) {
		E entity = null;
		try {
			entity = getService().get(id);
		} catch (IllegalArgumentException ex) {
			// simply not found
		}
		if (entity == null) {
			entity = getService().getByCode(id.toString());
		}
		return entity;
	}
}
