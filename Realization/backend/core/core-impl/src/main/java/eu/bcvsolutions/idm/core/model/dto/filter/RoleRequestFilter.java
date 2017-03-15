package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Filter for role request
 * 
 * @author svandav
 *
 */
public class RoleRequestFilter extends QuickFilter {
	private UUID applicantId;
	private String applicant;
	private RoleRequestState state;
	private UUID duplicatedToRequestId;
	private RoleRequestState notState;

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

	public RoleRequestState getNotState() {
		return notState;
	}

	public void setNotState(RoleRequestState notState) {
		this.notState = notState;
	}

}
