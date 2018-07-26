package eu.bcvsolutions.idm.core.rest;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.rest.impl.IdmRequestController;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * CRUD operations for requests
 * 
 * @author svandav
 */
public abstract class AbstractRequestDtoController<DTO extends Requestable, F extends BaseFilter>
		extends AbstractReadWriteDtoController<DTO, F> {

	@Autowired
	private RequestManager requestManager;

	public AbstractRequestDtoController(ReadWriteDtoService<DTO, F> entityService) {
		super(entityService);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @param requestId
	 * @return
	 */
	@ApiOperation(value = "Create / update record", authorizations = {
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> post(@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record (dto).", required = true) DTO dto) { //
		Requestable resultDto = requestManager.post(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Update DTO by given backendId and convert to response
	 * 
	 * @param requestId
	 * @param backendId
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Update record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) })
	public ResponseEntity<?> put(@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record's uuid identifier or unique code", required = true) String backendId, //
			@ApiParam(value = "Record (dto).", required = true) DTO dto) { //
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		Requestable resultDto = requestManager.post(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param requestId
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "Delete record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) })
	public ResponseEntity<?> delete(@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record's uuid identifier or unique code.", required = true) String backendId) { //
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		Requestable resultDto = requestManager.delete(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @param requestId
	 * @return
	 */
	@ApiOperation(value = "Read record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> get(@PathVariable @NotNull String requestId,
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true) //
			@PathVariable @NotNull String backendId) { //

		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		Requestable resultDto = requestManager.get(requestId, dto);
		if (resultDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@ApiOperation(value = "Create request for DTO", authorizations = {
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> createRequest(@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		IdmRequestDto request = requestManager.createRequest(dto);
		Link selfLink = ControllerLinkBuilder.linkTo(IdmRequestController.class).slash(request.getId()).withSelfRel();
		Resource<IdmRequestDto> resource = new Resource<IdmRequestDto>(request, selfLink);
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	protected ResourceSupport toResource(String requestId, DTO dto) {
		if (dto == null) {
			return null;
		}
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()) //
				.slash(requestId) //
				.slash(this.getRequestSubPath()) //
				.slash(dto.getId()).withSelfRel(); //
		Resource<DTO> resourceSupport = new Resource<DTO>(dto, selfLink);
		return resourceSupport;
	}

	public abstract String getRequestSubPath();
}
