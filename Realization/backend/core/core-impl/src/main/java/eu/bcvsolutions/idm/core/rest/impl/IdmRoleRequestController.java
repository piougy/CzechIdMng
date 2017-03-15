package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
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
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRoleRequestDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, @RequestBody @NotNull IdmRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	
	@Override
	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_DELETE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
//	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
//			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	public ResponseEntity<?> startRequest(@PathVariable @NotNull String backendId) {
		((IdmRoleRequestService)this.getService()).startRequest(UUID.fromString(backendId));
		return this.get(backendId);
	}

	
	@Override
	protected RoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleRequestFilter filter = new RoleRequestFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setApplicant(getParameterConverter().toString(parameters, "applicant"));
		filter.setApplicantId(getParameterConverter().toUuid(parameters, "applicantId"));

		if (filter.getApplicant() != null) {
			try {
				// Applicant can be UUID (Username vs UUID identification
				// schizma)
				filter.setApplicantId(UUID.fromString(filter.getApplicant()));
				filter.setApplicant(null);
			} catch (IllegalArgumentException ex) {
				// ok applicant is not UUID
			}
		}
		
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		filter.setNotState(getParameterConverter().toEnum(parameters, "notState", RoleRequestState.class));
		return filter;
	}
	
}
