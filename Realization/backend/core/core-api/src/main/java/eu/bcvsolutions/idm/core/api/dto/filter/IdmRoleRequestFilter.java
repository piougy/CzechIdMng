package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
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
	private List<OperationState> resultStates;
	private UUID duplicatedToRequestId;
	private List<RoleRequestState> states;
	private DateTime createdFrom;
	private DateTime createdTill;
	private List<UUID> applicants;
	private UUID creatorId;
	private boolean includeConcepts = false;
	/**
	 * If true, then returns requests where state in IdM and state on a systems is
	 * EXECUTED. 
	 * If Boolean.FALSE, then return all requests where IdM state is not
	 * DUPLICATED, CANCELED, DISAPPROVED and IdM state is not EXECUTED or system
	 * state is not EXECUTED and not null.
	 */
	private Boolean executed;

	public IdmRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleRequestFilter(boolean includeConcepts) {
		this(new LinkedMultiValueMap<>());
		this.includeConcepts = includeConcepts;
	}

	public IdmRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleRequestDto.class, data);
	}
	
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
	
	public void setCreatorId(UUID creatorId) {
		this.creatorId = creatorId;
	}
	
	public UUID getCreatorId() {
		return creatorId;
	}

	public boolean isIncludeConcepts() {
		return includeConcepts;
	}

	public void setIncludeConcepts(boolean includeConcepts) {
		this.includeConcepts = includeConcepts;
	}

	public Boolean getExecuted() {
		return executed;
	}

	public void setExecuted(Boolean executed) {
		this.executed = executed;
	}

	public List<OperationState> getResultStates() {
		return resultStates;
	}

	public void setResultStates(List<OperationState> resultStates) {
		this.resultStates = resultStates;
	}
}
