package eu.bcvsolutions.idm.core.api.rest;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * CRUD operations for DTO
 * 
 * @see DataFilter
 * @param <DTO> dto type
 * @param <F> filter type
 * @author Svanda
 * @author Radek Tomiška
 */
public abstract class AbstractReadWriteDtoController<DTO extends BaseDto, F extends BaseFilter>
		extends AbstractReadDtoController<DTO, F> {
	
	@Autowired(required = false) // optional dependency for support patch method
	private RequestResourceResolver requestResourceResolver;

	public AbstractReadWriteDtoController(ReadWriteDtoService<DTO, F> entityService) {
		super(entityService);
	}
	
	/**
	 * Invokes JSR 303 validations programmatically (its needed for patch method).
	 * Its called before save method.
	 *  
	 * @param entity
	 * @return
	 */
	protected DTO validateDto(DTO dto) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    Validator validator = factory.getValidator();
		Set<ConstraintViolation<DTO>> errors = validator.validate(dto);
		if(!errors.isEmpty()) {
			throw new ConstraintViolationException("Validation failed for dto [" + getDtoClass().getSimpleName() +"]", errors);
		}
		return dto;
	}
	
	/**
	 * Save (Creates / updates) given DTO - are called by putDto / postDto / patchDto methods
	 * 
	 * @param dto
	 * @return
	 */
	public DTO saveDto(DTO dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required");
		//
		return getService().save(validateDto(dto), permission);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Create / update record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> post(@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		return new ResponseEntity<>(toResource(postDto(dto)), HttpStatus.CREATED);
	}

	/**
	 * Creates / updates given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO postDto(DTO dto) {
		ReadWriteDtoService<DTO, F> service = getService();
		//
		return saveDto(dto, service.isNew(dto) ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE);
	}

	/**
	 * Update DTO by given backendId and convert to response
	 * 
	 * @param backendId
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Update record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> put(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports <pre>Codeable</pre> interface.", required = true) 
			String backendId,
			@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		DTO updatedDto = putDto(dto);
		return new ResponseEntity<>(toResource(updatedDto), HttpStatus.OK);
	}

	/**
	 * Updates given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO putDto(DTO dto) {		
		return saveDto(dto, IdmBasePermission.UPDATE);
	}

	/**
	 * Patch is not implemented yet
	 * 
	 * @param backendId
	 * @param nativeRequest
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> patch(String backendId, HttpServletRequest nativeRequest) 
			throws HttpMessageNotReadableException {
		if (requestResourceResolver == null) {
			throw new ResultCodeException(CoreResultCode.NOT_SUPPORTED, ImmutableMap.of("method", "patch method"));
		}
		//
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		updateDto = patchDto((DTO) requestResourceResolver.resolve(nativeRequest, getDtoClass(), updateDto));
		return new ResponseEntity<>(toResource(updateDto), HttpStatus.OK);
	}
	
	/**
	 * Patch given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO patchDto(DTO dto) {
		return saveDto(dto, IdmBasePermission.UPDATE);
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "Delete record", authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports <pre>Codeable</pre> interface.", required = true)
			String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		deleteDto(dto);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Deletes given DTO
	 * 
	 * @param backendId
	 * @return
	 */
	public void deleteDto(DTO dto) {
		Assert.notNull(dto, "DTO is required");
		//
		getService().delete(dto, IdmBasePermission.DELETE);
	}

	/**
	 * Returns DTO service configured to current controller
	 */
	@Override
	protected ReadWriteDtoService<DTO, F> getService() {
		return (ReadWriteDtoService<DTO, F>) super.getService();
	}
}
