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
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Tree account
 * @author svandav
 *
 */
@Entity
@Table(name = "acc_tree_account", indexes = {
		@Index(name = "idx_acc_tree_account_acc", columnList = "account_id"),
		@Index(name = "idx_acc_tree_account_tree", columnList = "tree_node_id") })
public class AccTreeAccount extends AbstractEntity implements EntityAccount {

	private static final long serialVersionUID = 1356548381619742855L;

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
	@JoinColumn(name = "tree_node_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmTreeNode treeNode;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "role_system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysRoleSystem roleSystem;

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

	public SysRoleSystem getRoleSystem() {
		return roleSystem;
	}

	public void setRoleSystem(SysRoleSystem roleSystem) {
		this.roleSystem = roleSystem;
	}

	public IdmTreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(IdmTreeNode treeNode) {
		this.treeNode = treeNode;
	}
	
	@Override
	public AbstractEntity getEntity(){
		return this.treeNode;
	}
}
