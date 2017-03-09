package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

/**
 * Role request endpoint
 * @author svandav
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/role-requests")
public class IdmRoleRequestController extends DefaultReadWriteDtoController<IdmRoleRequestDto, RoleRequestFilter>{

	
	@Autowired
	public IdmRoleRequestController(
			IdmRoleRequestService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> create(@RequestBody @NotNull IdmRoleRequestDto dto) {
		return super.create(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, @RequestBody @NotNull IdmRoleRequestDto dto) {
		return super.update(backendId, dto);
	}

	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_DELETE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	
	@Override
	protected RoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleRequestFilter filter = new RoleRequestFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setApplicantId(getParameterConverter().toUuid(parameters, "applicantId"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		return filter;
	}
	
}
