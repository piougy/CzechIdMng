package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Concept role request endpoint
 * 
 * TODO: secure endpoint (+ generalize AbstractReadWriteDtoController)
 * 
 * @author svandav
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/concept-role-requests")
@Api(
		value = IdmConceptRoleRequestController.TAG, 
		description = "Operations with single roles in request", 
		tags = { IdmConceptRoleRequestController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmConceptRoleRequestController
		extends DefaultReadWriteDtoController<IdmConceptRoleRequestDto, ConceptRoleRequestFilter> {

	protected static final String TAG = "Role Request - concepts";
	
	@Autowired
	public IdmConceptRoleRequestController(IdmConceptRoleRequestService service) {
		super(service);
	}

	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_READ + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@ApiOperation(
			value = "Concept detail", 
			nickname = "getConceptRoleRequest", 
			response = IdmConceptRoleRequestDto.class, 
			tags = { IdmConceptRoleRequestController.TAG })
	public ResponseEntity<?> get(
			@ApiParam(value = "Concept's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@ApiOperation(
			value = "Create / update concept", 
			nickname = "postConceptRoleRequest",  
			response = IdmConceptRoleRequestDto.class, 
			tags = { IdmConceptRoleRequestController.TAG })
	public ResponseEntity<?> post(@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@ApiOperation(
			value = "Update concept", 
			nickname = "putConceptRoleRequest",  
			response = IdmConceptRoleRequestDto.class, 
			tags = { IdmConceptRoleRequestController.TAG })
	public ResponseEntity<?> put(
			@ApiParam(value = "Concept's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_DELETE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@ApiOperation(
			value = "Delete concept", 
			nickname = "delete ConceptRoleRequest",
			tags = { IdmConceptRoleRequestController.TAG })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Concept's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	protected ConceptRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		ConceptRoleRequestFilter filter = new ConceptRoleRequestFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		return filter;
	}
}
