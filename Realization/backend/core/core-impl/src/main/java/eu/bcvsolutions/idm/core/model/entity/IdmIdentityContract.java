package eu.bcvsolutions.idm.core.model.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Identity contract - working position
 * 
 * @author Radek Tomiška
 */
@Entity
@Table(name = "idm_identity_contract", indexes = {
		@Index(name = "idx_idm_identity_contract_guarantee", columnList = "guarantee_id"),
		@Index(name = "idx_idm_identity_contract_identity", columnList = "identity_id"),
		@Index(name = "idx_idm_identity_contract_working_position", columnList = "working_position_id")})
public class IdmIdentityContract extends AbstractEntity implements ValidableEntity {

	private static final long serialVersionUID = 328041550861866181L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id")
	private IdmIdentity identity;
	
	@Audited
	@Column(name = "valid_from")
	@Temporal(TemporalType.DATE)
	private Date validFrom;
	
	@Audited
	@Column(name = "valid_till")
	@Temporal(TemporalType.DATE)
	private Date validTill;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "guarantee_id", referencedColumnName = "id")
	private IdmIdentity guarantee;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "working_position_id", referencedColumnName = "id")
	private IdmTreeNode workingPosition;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "position", length = DefaultFieldLengths.NAME)
	private String position; // string position - if working position tree is not configured, then this string could be used
	
	public IdmIdentityContract() {
	}
	
	public IdmIdentityContract(UUID id) {
		super(id);
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTo) {
		this.validTill = validTo;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public IdmTreeNode getWorkingPosition() {
		return workingPosition;
	}

	public void setWorkingPosition(IdmTreeNode workingPosition) {
		this.workingPosition = workingPosition;
	}

	public IdmIdentity getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return position;
	}
}
