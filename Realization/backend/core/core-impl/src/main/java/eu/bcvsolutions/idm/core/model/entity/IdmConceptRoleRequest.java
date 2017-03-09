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

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;

/**
 * Concept for requested role.
 * @author svandav
 *
 */
@Entity
@Table(name = "idm_concept_role_request", indexes = {
		@Index(name = "idx_idm_conc_role_ident_c", columnList = "identity_contract_id"),
		@Index(name = "idx_idm_conc_role_request", columnList = "request_role_id"),
		@Index(name = "idx_idm_conc_role_role", columnList = "role_id")
})
public class IdmConceptRoleRequest extends AbstractEntity implements ValidableEntity {

	
	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "request_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRoleRequest roleRequest;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityContract identityContract;
	
	@NotNull
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityRole identityRole;
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited
	@Column(name = "operation")
	@Enumerated(EnumType.STRING)
	private ConceptRoleRequestOperation operation;
	
	@Audited
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private RoleRequestState state;
	
	@Audited
	@Column(name = "wf_process_id")
	private String wfProcessId;

	public IdmRoleRequest getRoleRequest() {
		return roleRequest;
	}

	public void setRoleRequest(IdmRoleRequest roleRequest) {
		this.roleRequest = roleRequest;
	}

	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public ConceptRoleRequestOperation getOperation() {
		return operation;
	}

	public void setOperation(ConceptRoleRequestOperation operation) {
		this.operation = operation;
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

	public IdmIdentityRole getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(IdmIdentityRole identityRole) {
		this.identityRole = identityRole;
	}
	
	
}