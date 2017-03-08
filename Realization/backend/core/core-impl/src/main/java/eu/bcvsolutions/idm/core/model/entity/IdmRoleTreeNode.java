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

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.domain.RecursionType;

/**
 * Automatic roles for tree structures
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role_tree_node", indexes = {
		@Index(name = "ux_idm_role_tree_node", columnList = "role_id,tree_node_id,recursion_type", unique = true),
		@Index(name = "idx_idm_role_tree_role", columnList = "role_id"), 
		@Index(name = "idx_idm_role_tree_node", columnList = "tree_node_id") })
public class IdmRoleTreeNode extends AbstractEntity {

	private static final long serialVersionUID = 6000961264258576244L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "tree_node_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode treeNode;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "recursion_type", nullable = false)
	private RecursionType recursionType = RecursionType.NO;
	
	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public IdmTreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(IdmTreeNode treeNode) {
		this.treeNode = treeNode;
	}
	
	public void setRecursionType(RecursionType recursionType) {
		this.recursionType = recursionType;
	}
	
	public RecursionType getRecursionType() {
		return recursionType;
	}
}
