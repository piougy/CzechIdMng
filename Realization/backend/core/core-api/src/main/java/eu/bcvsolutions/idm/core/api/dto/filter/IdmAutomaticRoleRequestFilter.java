package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;

/**
 * Filter for automatic role request
 *
 * @author svandav
 */
public class IdmAutomaticRoleRequestFilter extends DataFilter {
	
	private UUID roleId;
	private List<RequestState> states;
	private UUID automaticRoleId;
	private AutomaticRoleRequestType requestType;
	private String role; // role code

	public IdmAutomaticRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmAutomaticRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmAutomaticRoleRequestDto.class, data);
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public UUID getAutomaticRoleId() {
		return automaticRoleId;
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.automaticRoleId = automaticRoleId;
	}

	public AutomaticRoleRequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(AutomaticRoleRequestType requestType) {
		this.requestType = requestType;
	}

	public List<RequestState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}

	public void setStates(List<RequestState> states) {
		this.states = states;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
