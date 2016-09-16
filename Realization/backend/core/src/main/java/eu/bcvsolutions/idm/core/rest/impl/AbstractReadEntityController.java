package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

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
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadEntityService;

public abstract class AbstractReadEntityController<E extends BaseEntity> {
	
	private ReadEntityService<E> entityService;
	
	public AbstractReadEntityController(ReadEntityService<E> entityService) {
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
	
	protected ReadEntityService<E> getEntityService() {
		return (ReadEntityService<E>)entityService;
	}
	
	public ResponseEntity<?> getItemResource(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		E entity = readItemResource(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}		
		return new ResponseEntity<>(assembleEntity(entity, assembler), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	protected E readItemResource(String backendId) {
		if(getEntityLookup() == null) {
			// TODO: backendId conversion
			return getEntityService().get(Long.valueOf(backendId));
		}		
		return (E) getEntityLookup().lookupEntity(backendId);
	}
	
	@SuppressWarnings("unchecked")
	public Resources<?> searchCollectionResource(@PageableDefault Pageable pageable, 
			@RequestParam MultiValueMap<String, Object> parameters, 
			PagedResourcesAssembler<Object> pagedAssembler, PersistentEntityResourceAssembler assembler) {
		if (getAssembler() == null) {
			return pagedAssembler.toResource((Page<Object>)getEntityService().find(null, pageable), assembler);
		} else {
			return pagedAssembler.toResource((Page<Object>)getEntityService().find(null, pageable), getAssembler());
		}
	}
	
	public Resources<?> searchQuickCollectionResource(@PageableDefault Pageable pageable, 
			@RequestParam MultiValueMap<String, Object> parameters, 
			PagedResourcesAssembler<Object> pagedAssembler,
			PersistentEntityResourceAssembler assembler) {
		return searchCollectionResource(pageable, parameters, pagedAssembler, assembler);
	}
	
	// TODO: toResource
	@SuppressWarnings("rawtypes")
	protected ResourceSupport assembleEntity(E entity, PersistentEntityResourceAssembler assembler) {
		if(getAssembler() != null) {
			return getAssembler().toResource(entity);
		}
		return assembler.toFullResource(entity);
	}
	
}
