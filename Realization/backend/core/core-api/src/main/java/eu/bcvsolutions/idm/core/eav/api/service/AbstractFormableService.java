package eu.bcvsolutions.idm.core.eav.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Abstract implementation for generic CRUD operations on a repository for a specific type with extended attributes. 
 * Owner type has to support event processing.
 * 
 * @author Radek Tomiška
 *
 * @param <DTO> dto type
 * @param <E> {@link FormableEntity} type which supports extended atributes
 * @param <F> {@link BaseFilter} type
 */
public abstract class AbstractFormableService<DTO extends BaseDto, E extends FormableEntity, F extends BaseFilter> 
		extends AbstractEventableDtoService<DTO, E, F> {

	private final FormService formService;
	
	@Autowired
	public AbstractFormableService(AbstractEntityRepository<E, F> repository, EntityEventManager entityEventManager, FormService formService) {
		super(repository, entityEventManager);
		//
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
	public void deleteInternal(DTO dto) {
		formService.deleteValues(dto);
		//
		super.delete(dto);
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
