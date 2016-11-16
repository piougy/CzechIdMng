package eu.bcvsolutions.idm.eav.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.service.FormService;

/**
 * Abstract implementation for generic CRUD operations on a repository for a specific type with extended attributes.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link FormableEntity} type which supports extended atributes
 * @param <F> {@link BaseFilter} type
 */
public abstract class AbstractFormableService<E extends FormableEntity, F extends BaseFilter> extends AbstractReadWriteEntityService<E, F> {

	private final FormService formService;
	
	@Autowired
	public AbstractFormableService(FormService formService) {
		Assert.notNull(formService);
		//
		this.formService = formService;
	}
	
	/**
	 * Deletes a given entity with all extended attributes
	 * 
	 * @param entity
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void delete(E entity) {
		formService.deleteValues(entity);
		//
		super.delete(entity);
	}
	
	/**
	 * Returns form service
	 * 
	 * @return
	 */
	protected FormService getFormService() {
		return formService;
	}
	
}
