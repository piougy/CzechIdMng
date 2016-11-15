package eu.bcvsolutions.idm.core.api.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * CRUD operations
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractReadWriteEntityController<E extends BaseEntity, F extends BaseFilter> extends AbstractReadEntityController<E, F> {
	
	@Autowired
	private RequestResourceResolver requestResourceResolver;
	
	public AbstractReadWriteEntityController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	public AbstractReadWriteEntityController(EntityLookupService entityLookupService, ReadWriteEntityService<E, F> entityService) {
		super(entityLookupService, entityService);
	}
	
	/**
	 * Creates entity from given nativeRequest
	 * 
	 * @param nativeRequest
	 * @param assembler
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {		
		E createdIdentity = createEntity((E) requestResourceResolver.resolve(nativeRequest, getEntityClass(), null));
		return new ResponseEntity<>(toResource(createdIdentity, assembler), HttpStatus.CREATED);
	}
	
	/**
	 * Creates given entity
	 * 
	 * @param entity
	 * @return
	 */
	public E createEntity(E entity) {
		// TODO: events
		return getEntityService().save(entity);
	}
	
	/**
	 * Updates entity from given nativeRequest by given backendId
	 * 
	 * @param backendId
	 * @param nativeRequest
	 * @param assembler
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> update(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		E updatedEntity = updateEntity((E) requestResourceResolver.resolve(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	/**
	 * Updates given entity
	 * 
	 * @param entity
	 * @return
	 */
	public E updateEntity(E entity) {
		Assert.notNull(entity, "Entity is required");		
		return getEntityService().save(entity);
	}
	
	/**
	 * Updates (patch) given entity - only given entity fields in nativeRequest is updated.
	 * 
	 * @param backendId
	 * @param nativeRequest
	 * @param assembler
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> patch(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		E updatedEntity = patchEntity((E) requestResourceResolver.resolve(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	/**
	 * Updates given entity.
	 * 
	 * TODO: is this redundant? Or diferent event will be thrown in future?
	 * 
	 * @param entity
	 * @return
	 */
	public E patchEntity(E entity) {
		Assert.notNull(entity, "Entity is required");
		return getEntityService().save(entity);
	}
	
	/**
	 * Deletes entity by given id
	 * 
	 * @param backendId
	 * @return
	 */
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		deleteEntity(entity);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Deletes given entity
	 * 
	 * @param backendId
	 * @return
	 */
	public void deleteEntity(E entity) {
		Assert.notNull(entity, "Entity is required");
		getEntityService().delete(entity);
	}
	
	/**
	 * Returns entity service configured to current controller
	 */
	@Override
	protected ReadWriteEntityService<E, F> getEntityService() {
		return (ReadWriteEntityService<E, F>) super.getEntityService();
	}

}
