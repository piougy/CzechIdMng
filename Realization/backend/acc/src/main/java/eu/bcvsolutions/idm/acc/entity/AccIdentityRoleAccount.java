package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/*
 * Identity-role account
 * 
 */
@Entity
@Table(name = "acc_identity_role_account", indexes = {
		@Index(name = "idx_acc_iderole_acc_acc", columnList = "account_id"),
		@Index(name = "idx_acc_iderole_acc_contr", columnList = "identity_role_id") })
public class AccIdentityRoleAccount extends AbstractEntity implements EntityAccount {

	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccount account;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityRole identityRole;

	@Audited
	@NotNull
	@Column(name = "ownership", nullable = false)
	private boolean ownership = true;

	@Override
	public AccAccount getAccount() {
		return account;
	}

	public void setAccount(AccAccount account) {
		this.account = account;
	}

	@Override
	public boolean isOwnership() {
		return ownership;
	}

	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}

	public IdmIdentityRole getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(IdmIdentityRole identityRole) {
		this.identityRole = identityRole;
	}

	@Override
	public AbstractEntity getEntity(){
		return this.identityRole;
	}
}
