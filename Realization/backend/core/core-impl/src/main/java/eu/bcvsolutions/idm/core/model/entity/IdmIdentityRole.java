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
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

@Entity
@Table(name = "idm_identity_role", indexes = {
		@Index(name = "idx_idm_identity_role_identity", columnList = "identity_id"),
		@Index(name = "idx_idm_identity_role_role", columnList = "role_id")
})
public class IdmIdentityRole extends AbstractEntity implements ValidableEntity {

	private static final long serialVersionUID = 9208706652291035265L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id")
	private IdmIdentity identity;
	
	@NotNull
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private IdmRole role;
	
	@Audited
	@Column(name = "valid_from")
	@Temporal(TemporalType.DATE)
	private Date validFrom;
	
	@Audited
	@Column(name = "valid_till")
	@Temporal(TemporalType.DATE)
	private Date validTill;

	public IdmIdentityRole() {
	}

	public IdmIdentityRole(UUID id) {
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

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}
}