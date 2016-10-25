package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/*
 * Identity account
 * 
 */
@Entity
@Table(name = "acc_identity_account", indexes = {
		@Index(name = "ux_identity_account", columnList = "identity_id,account_id", unique = true),
		@Index(name = "idx_acc_identity_account_account_id", columnList = "account_id"),
		@Index(name = "idx_acc_identity_account_identity_id", columnList = "identity_id"),
		@Index(name = "idx_acc_identity_identity_role_id", columnList = "identity_role_id")
		})
public class AccIdentityAccount extends AbstractEntity {

	private static final long serialVersionUID = 1356548381619742855L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AccAccount account;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private IdmIdentity identity;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_role_id", referencedColumnName = "id")
	private IdmIdentityRole identityRole; // identity account is based on identity role asing and  system mapping
	
	@Audited
	@NotNull
	@Column(name = "ownership", nullable = false)
	private boolean ownership = true;

	public AccAccount getAccount() {
		return account;
	}

	public void setAccount(AccAccount account) {
		this.account = account;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public boolean isOwnership() {
		return ownership;
	}

	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}
	
	public void setIdentityRole(IdmIdentityRole identityRole) {
		this.identityRole = identityRole;
	}
	
	public IdmIdentityRole getIdentityRole() {
		return identityRole;
	}
}
