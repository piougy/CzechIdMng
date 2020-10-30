package eu.bcvsolutions.idm.core.rest.impl.projection;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.event.IdentityProjectionEvent;
import eu.bcvsolutions.idm.core.eav.api.event.IdentityProjectionEvent.IdentityProjectionEventType;
import eu.bcvsolutions.idm.core.eav.api.service.IdentityProjectionManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Projection controller - get & post is supported only.
 * 
 * TODO: generate (password generator?) / validate endpoint + support
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-projection")
@Api(
		value = IdmIdentityProjectionController.TAG,  
		tags = { IdmIdentityProjectionController.TAG }, 
		description = "Operations with identity projection",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmIdentityProjectionController implements BaseDtoController<IdmIdentityProjectionDto> {

	protected static final String TAG = "Identity projection";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdentityProjectionManager identityProjectionManager;

	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity projection detail", 
			nickname = "getIdentityProjection", 
			response = IdmIdentityProjectionDto.class, 
			tags = { IdmIdentityProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmIdentityProjectionDto dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(identityService.getEntityClass(), backendId);
		}
		ResourceSupport resource = toResource(dto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@ApiOperation(
			value = "Create / update identity projection", 
			nickname = "postIdentity", 
			response = IdmIdentityProjectionDto.class, 
			tags = { IdmIdentityProjectionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityProjectionDto dto) {
		ResourceSupport resource = toResource(postDto(dto));
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}
	
	protected IdmIdentityProjectionDto getDto(Serializable backendId) {
		return identityProjectionManager.get(backendId, IdmBasePermission.READ);
	}
	
	protected IdmIdentityProjectionDto postDto(IdmIdentityProjectionDto dto) {
		boolean isNew = identityService.isNew(dto.getIdentity());
		IdentityProjectionEvent event;
		// create
		if (isNew) {
			event = new IdentityProjectionEvent(IdentityProjectionEventType.CREATE, dto);
		} else {
			// update
			event = new IdentityProjectionEvent(IdentityProjectionEventType.UPDATE, dto);
		}
		event.setPriority(PriorityType.HIGH);
		//
		dto = identityProjectionManager.publish(event, isNew ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE).getContent();
		// => load eav and permission is needed
		return getDto(dto);
	}
	
	protected ResourceSupport toResource(IdmIdentityProjectionDto dto) {
		if(dto == null) { 
			return null;
		} 
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		//
		return new Resource<IdmIdentityProjectionDto>(dto, selfLink);
	}
}
