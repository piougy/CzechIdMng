package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.PersistentEntityResolver;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

public abstract class AbstractReadWriteController<E extends BaseEntity> extends AbstractReadEntityController<E> {
	
	@Autowired
	private PersistentEntities persistentEntities; 
	
	@Autowired
	private Associations associationLinks;
	
	private PersistentEntityResolver resolver;
	
	@Autowired
	private List<HttpMessageConverter<?>> messageConverters;
	
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
	public AbstractReadWriteController(ReadWriteEntityService<E> entityService) {
		super(entityService);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> postCollectionResource(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws Exception {		
		E createdIdentity = getEntityService().save((E)getResolver().resolveEntity(nativeRequest, getEntityService().getEntityClass(), null));
		return new ResponseEntity<>(assembleEntity(createdIdentity, assembler), HttpStatus.CREATED);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> putItemResource(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws Exception {
		E updateEntity = readItemResource(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		E updatedEntity = getEntityService().save((E)getResolver().resolveEntity(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(assembleEntity(updatedEntity, assembler), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> patchItemResource(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws Exception {
		E updateEntity = readItemResource(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}		
		E updatedEntity = getEntityService().save((E)getResolver().resolveEntity(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(assembleEntity(updatedEntity, assembler), HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteItemResource(@PathVariable @NotNull String backendId) {
		E entity = readItemResource(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		getEntityService().delete(entity);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	protected ReadWriteEntityService<E> getEntityService() {
		return (ReadWriteEntityService<E>) super.getEntityService();
	}
	
	private PersistentEntityResolver getResolver() {
		if (resolver == null) {
			resolver = new PersistentEntityResolver(messageConverters, new DomainObjectReader(persistentEntities, associationLinks));
		}
		return resolver;
	}

}
