package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

/**
 * Role request endpoint
 * @author svandav
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-requests")
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
		if (RoleRequestedByType.AUTOMATICALLY == dto.getRequestedByType()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_AUTOMATICALLY_NOT_ALLOWED,
					ImmutableMap.of("new", dto));
		}
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
		IdmRoleRequestService service = ((IdmRoleRequestService)this.getService());
		IdmRoleRequestDto dto = service.get(backendId);
		// Request in Executed state can not be delete or change
		if(RoleRequestState.EXECUTED == dto.getState()){
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}
		
		// Only request in Concept state, can be deleted. In others states, will be request set to Canceled state and save.
		if(RoleRequestState.CONCEPT == dto.getState()){
			service.delete(dto);
		}else {
			service.cancel(dto);
		}
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
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
		String statesStr = getParameterConverter().toString(parameters, "states");
		if(!Strings.isNullOrEmpty(statesStr)){
			List<RoleRequestState> states = new ArrayList<>();
			for( String state : statesStr.split(",")){
				String stateTrimmed = state.trim();
				if(!Strings.isNullOrEmpty(stateTrimmed)){
					states.add(RoleRequestState.valueOf(stateTrimmed));
				}
			}
			if(!states.isEmpty()){
				filter.setStates(states);
			}
		}
		return filter;
	}
	
}
