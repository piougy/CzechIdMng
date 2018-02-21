package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Request for roles
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "idm_role_request", indexes = {
		@Index(name = "idx_idm_role_request_app_id", columnList = "applicant_id"),
		@Index(name = "idx_idm_role_request_state", columnList = "state")
})
public class IdmRoleRequest extends AbstractEntity{

	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "applicant_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentity applicant;

	@Audited
	@NotNull
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private RoleRequestState state = RoleRequestState.CONCEPT;

	@Audited
	@NotNull
	@Column(name = "requested_by_type")
	@Enumerated(EnumType.STRING)
	private RoleRequestedByType requestedByType = RoleRequestedByType.MANUALLY;

	@Audited
	@Column(name = "wf_process_id")
	private String wfProcessId;

	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "original_request")
	private String originalRequest;

	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "log")
	private String log;

	@Audited
	@NotNull
	@Column(name = "execute_immediately")
	private boolean executeImmediately = false;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "duplicated_to_request", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = true)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRoleRequest duplicatedToRequest;
	
	@Audited
	@Column(name = "description")
	private String description;

	public IdmIdentity getApplicant() {
		return applicant;
	}

	public void setApplicant(IdmIdentity applicant) {
		this.applicant = applicant;
	}

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

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public IdmRoleRequest getDuplicatedToRequest() {
		return duplicatedToRequest;
	}

	public void setDuplicatedToRequest(IdmRoleRequest duplicatedToRequest) {
		this.duplicatedToRequest = duplicatedToRequest;
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


}