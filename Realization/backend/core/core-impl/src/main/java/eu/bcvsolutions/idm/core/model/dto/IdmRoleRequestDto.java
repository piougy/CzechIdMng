package eu.bcvsolutions.idm.core.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestedByType;

/**
 * Dto for role request
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "roleRequests")
public class IdmRoleRequestDto extends AbstractDto implements Loggable {

	private static final long serialVersionUID = 1L;
	public static final String WF_PROCESS_FIELD = "wfProcessId";

	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID applicant;
	@JsonProperty(access = Access.READ_ONLY)
	private RoleRequestState state;
	private RoleRequestedByType requestedByType;
	@JsonProperty(access = Access.READ_ONLY)
	//In embedded map, is under wfProcessId key actual task - WorkflowHistoricTaskInstanceDto.class
	private String wfProcessId;
	@JsonProperty(access = Access.READ_ONLY)
	private String originalRequest;
	@JsonProperty(access = Access.READ_ONLY)
	private List<IdmConceptRoleRequestDto> conceptRoles;
	private boolean executeImmediately = false;
	@Embedded(dtoClass = IdmRoleRequestDto.class)
	private UUID duplicatedToRequest;
	@JsonProperty(access = Access.READ_ONLY)
	private String log;
	private String description;

	public RoleRequestState getState() {
		return state;
	}

	public void setState(RoleRequestState state) {
		this.state = state;
	}

	public String getWfProcessId() {
		return wfProcessId;
	}

	public void setWfProcessId(String wfProcessId) {
		this.wfProcessId = wfProcessId;
	}

	public String getOriginalRequest() {
		return originalRequest;
	}

	public void setOriginalRequest(String originalRequest) {
		this.originalRequest = originalRequest;
	}

	public UUID getApplicant() {
		return applicant;
	}

	public void setApplicant(UUID applicant) {
		this.applicant = applicant;
	}

	public List<IdmConceptRoleRequestDto> getConceptRoles() {
		if (conceptRoles == null) {
			conceptRoles = new ArrayList<>();
		}
		return conceptRoles;
	}

	public void setConceptRoles(List<IdmConceptRoleRequestDto> conceptRoles) {
		this.conceptRoles = conceptRoles;
	}

	public UUID getDuplicatedToRequest() {
		return duplicatedToRequest;
	}

	public void setDuplicatedToRequest(UUID duplicatedToRequest) {
		this.duplicatedToRequest = duplicatedToRequest;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public RoleRequestedByType getRequestedByType() {
		return requestedByType;
	}

	public void setRequestedByType(RoleRequestedByType requestedByType) {
		this.requestedByType = requestedByType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String addToLog(String text) {
		if (text != null) {
			StringBuilder builder = new StringBuilder();
			if (this.log != null) {
				builder.append(this.log);
				builder.append("\n" + LOG_SEPARATOR + "\n");
			}
			builder.append(text);
			this.setLog(builder.toString());
		}
		return this.getLog();
	}

}