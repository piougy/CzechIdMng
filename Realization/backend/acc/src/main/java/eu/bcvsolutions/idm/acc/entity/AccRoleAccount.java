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
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/*
 * Role account
 * 
 */
@Entity
@Table(name = "acc_role_account", indexes = {
		@Index(name = "idx_acc_role_account_acc", columnList = "account_id"),
		@Index(name = "idx_acc_role_account_role", columnList = "role_id") })
public class AccRoleAccount extends AbstractEntity implements EntityAccount {

	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private AccAccount account;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRole role;

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
	
	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	@Override
	public AbstractEntity getEntity(){
		return this.role;
	}
}
