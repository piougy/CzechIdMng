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
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;

/**
 * Concept role request endpoint
 * 
 * @author svandav
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/concept-role-requests")
public class IdmConceptRoleRequestController
		extends DefaultReadWriteDtoController<IdmConceptRoleRequestDto, ConceptRoleRequestFilter> {

	@Autowired
	public IdmConceptRoleRequestController(IdmConceptRoleRequestService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_READ + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> post(@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_WRITE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_REQUEST_DELETE + "') or hasAuthority('"
			+ IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
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
