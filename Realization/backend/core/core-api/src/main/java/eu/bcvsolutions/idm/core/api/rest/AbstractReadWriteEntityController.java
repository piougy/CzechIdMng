package eu.bcvsolutions.idm.core.api.rest;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * CRUD operations
 * 
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
 * @author Radek Tomiška
 * @deprecated use {@link AbstractReadWriteDtoController}
 */
@Deprecated
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
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {		
		E entity = (E) requestResourceResolver.resolve(nativeRequest, getEntityClass(), null);
		//
		checkAccess(entity, getEntityService().isNew(entity) ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE);
		//
		E createdIdentity = postEntity(validateEntity(entity));
		if (createdIdentity.getId() == null) {
			throw new ResultCodeException(CoreResultCode.ACCEPTED);
		}
		return new ResponseEntity<>(toResource(createdIdentity, assembler), HttpStatus.CREATED);
	}
	
	/**
	 * Creates given entity
	 * 
	 * @param entity
	 * @return
	 */
	public E postEntity(E entity) {
		Assert.notNull(entity, "Entity is required");	
		//
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
	public ResponseEntity<?> put(
			String backendId,
			HttpServletRequest nativeRequest, 
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(updateEntity, IdmBasePermission.UPDATE);
		//
		E updatedEntity = putEntity(validateEntity((E) requestResourceResolver.resolve(nativeRequest, getEntityService().getEntityClass(), updateEntity)));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	/**
	 * Updates given entity
	 * 
	 * @param entity
	 * @return
	 */
	public E putEntity(E entity) {
		Assert.notNull(entity, "Entity is required");	
		//
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
			String backendId,
			HttpServletRequest nativeRequest, 
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		E updateEntity = getEntity(backendId);
		if (updateEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
        checkAccess(updateEntity, IdmBasePermission.UPDATE);
        ;
		//
		E updatedEntity = patchEntity((E) requestResourceResolver.resolve(nativeRequest, getEntityService().getEntityClass(), updateEntity));
		return new ResponseEntity<>(toResource(updatedEntity, assembler), HttpStatus.OK);
	}
	
	/**
	 * Patch given entity.
	 * 
	 * @param entity
	 * @return
	 */
	public E patchEntity(E entity) {
		Assert.notNull(entity, "Entity is required");
		//
		return getEntityService().save(entity);
	}
	
	/**
	 * Invokes JSR 303 validations programatically
	 *  
	 * @param entity
	 * @return
	 */
	protected E validateEntity(E entity) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    Validator validator = factory.getValidator();
		Set<ConstraintViolation<E>> errors = validator.validate(entity);
		if(!errors.isEmpty()) {
			throw new ConstraintViolationException("Validation failed for entity [" + getEntityClass().getSimpleName() +"]", errors);
		}
		return entity;
	}
	
	/**
	 * Deletes entity by given id
	 * 
	 * @param backendId
	 * @return
	 */
	public ResponseEntity<?> delete(String backendId) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.DELETE);
		//
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
		//
		getEntityService().delete(entity);
	}
	
	/**
	 * Returns entity service configured to current controller
	 */
	@Override
	protected ReadWriteEntityService<E, F> getEntityService() {
		return (ReadWriteEntityService<E, F>) super.getEntityService();
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	public Set<String> permissions(@PathVariable @NotNull String backendId) {
		E entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return getAuthorizationManager().getPermissions(entity);
	}
}
