package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.dto.BaseFilter;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadEntityService;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

/**
 * Read operations (get, find)
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractReadEntityController<E extends BaseEntity, F extends BaseFilter> implements BaseEntityController<E> {
	
	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	
	private ReadEntityService<E, F> entityService;
	
	public AbstractReadEntityController(ReadEntityService<E, F> entityService) {
		this.entityService = entityService;
	}
	
	// TODO: Spring plugin
	@SuppressWarnings("rawtypes")
	protected ResourceAssemblerSupport<Object, ResourceWrapper> getAssembler() {
		return null;
	}
	
	// TODO: Spring plugin
	protected EntityLookup<E> getEntityLookup() {
		return null;
	}
	
	protected ReadEntityService<E, F> getEntityService() {
		return (ReadEntityService<E, F>)entityService;
	}
	
	protected Class<E> getEntityClass() {
		return getEntityService().getEntityClass();
	}
	
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}		
		return new ResponseEntity<>(toResource(entity, assembler), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public E getEntity(String backendId) {
		// TODO: read events
		if(getEntityLookup() == null) {
			return getEntityService().get(Long.valueOf(backendId));
		}		
		return (E) getEntityLookup().lookupEntity(backendId);
	}
	
	@SuppressWarnings("unchecked")
	public Resources<?> find(
			@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		// TODO: filter transform from parameters
		return toResources((Page<Object>)findEntities(toFilter(parameters), pageable), assembler);
	}
	
	public Page<E> findEntities(F filter, Pageable pageable) {
		// TODO: read event
		return getEntityService().find(filter, pageable);
	}	
	
	protected ResourceSupport toResource(E entity, PersistentEntityResourceAssembler assembler) {
		if(getAssembler() != null) {
			return getAssembler().toResource(entity);
		}
		return assembler.toFullResource(entity);
	}
	
	protected Resources<?> toResources(Page<Object> page, PersistentEntityResourceAssembler assembler) {
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, getEntityClass(), null); // TODO: base link
		}
		
		if (getAssembler() == null) {
			return pagedResourcesAssembler.toResource(page, assembler);
		} else {
			return pagedResourcesAssembler.toResource(page, getAssembler());
		}
	}
	
	/**
	 * Transforms request parameters to {@link BaseFilter}.
	 * @param parameters
	 * @return
	 */
	protected F toFilter(MultiValueMap<String, Object> parameters) {
		return null;
	}
}
