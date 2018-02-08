package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;

/**
 * Filter for role request
 * 
 * TODO: remove state field - use states
 *
 * @author svandav
 */
public class IdmRoleRequestFilter extends DataFilter {
	private UUID applicantId;
	private String applicant;
	private RoleRequestState state;
	private UUID duplicatedToRequestId;
	private List<RoleRequestState> states;
	private DateTime createdFrom;
	private DateTime createdTill;
	private List<UUID> applicants;

	public List<UUID> getApplicants() {
		if (applicants == null) {
			applicants = new ArrayList<>();
		}
		return applicants;
	}

	public void setApplicants(List<UUID> applicants) {
		this.applicants = applicants;
	}

	public DateTime getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(DateTime createdFrom) {
		this.createdFrom = createdFrom;
	}

	public DateTime getCreatedTill() {
		return createdTill;
	}

	public void setCreatedTill(DateTime createdTill) {
		this.createdTill = createdTill;
	}

	public IdmRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleRequestDto.class, data);
	}

	public UUID getApplicantId() {
		return applicantId;
	}

	public void setApplicantId(UUID applicantId) {
		this.applicantId = applicantId;
	}

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

	public String getApplicant() {
		return applicant;
	}

	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}

	public UUID getDuplicatedToRequestId() {
		return duplicatedToRequestId;
	}

	public void setDuplicatedToRequestId(UUID duplicatedToRequestId) {
		this.duplicatedToRequestId = duplicatedToRequestId;
	}

	public List<RoleRequestState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}

	public void setStates(List<RoleRequestState> states) {
		this.states = states;
	}

}
