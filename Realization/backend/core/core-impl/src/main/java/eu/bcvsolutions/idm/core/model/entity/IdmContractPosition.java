package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Identity's contract position - other work positions
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Entity
@Table(name = "idm_contract_position", indexes = {
		@Index(name = "idm_contract_position_contr", columnList = "identity_contract_id"),
		@Index(name = "idx_contract_position_pos", columnList = "work_position_id"),
		@Index(name = "idx_idm_contract_pos_ext_id", columnList = "external_id")})
public class IdmContractPosition extends AbstractEntity implements ExternalIdentifiable, AuditSearchable {

	private static final long serialVersionUID = 1L;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityContract identityContract;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "work_position_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode workPosition;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "position", length = DefaultFieldLengths.NAME)
	private String position; // string position - if working position tree is not configured, then this string could be used
	
	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}
	
	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}
	
	public IdmTreeNode getWorkPosition() {
		return workPosition;
	}

	public void setWorkPosition(IdmTreeNode workPosition) {
		this.workPosition = workPosition;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}

	@Override
	public String getOwnerId() {
		return this.getIdentityContract().getOwnerId();
	}

	@Override
	public String getOwnerCode() {
		return this.getIdentityContract().getOwnerCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getIdentityContract().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return this.getIdentityContract().getPosition();
	}

	@Override
	public String getSubOwnerType() {
		return IdmIdentityContract.class.getName();
	}
}
