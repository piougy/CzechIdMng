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

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * <i>SysSyncContractConfig</i> is responsible for keep specific informations about
 * contract synchronization configuration
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_contract_config", indexes = {
		@Index(name = "idx_sys_s_cont_conf_tree", columnList = "default_tree_type_id"),
		@Index(name = "idx_sys_s_cont_conf_node", columnList = "default_tree_node_id"),
		@Index(name = "idx_sys_s_cont_conf_lead", columnList = "default_leader_id")
		})
public class SysSyncContractConfig extends SysSyncConfig{


	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "default_tree_type_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeType defaultTreeType;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "default_tree_node_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmTreeNode defaultTreeNode;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "default_leader_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity defaultLeader;
	
	@Audited
	@NotNull
	@Column(name = "start_auto_role_rec", nullable = false)
	private boolean startAutoRoleRecalculation = true;
	
	@Audited
	@NotNull
	@Column(name = "start_hr_processes", nullable = false)
	private boolean startOfHrProcesses = true;

	public IdmTreeType getDefaultTreeType() {
		return defaultTreeType;
	}

	public void setDefaultTreeType(IdmTreeType defaultTreeType) {
		this.defaultTreeType = defaultTreeType;
	}

	public IdmTreeNode getDefaultTreeNode() {
		return defaultTreeNode;
	}

	public void setDefaultTreeNode(IdmTreeNode defaultTreeNode) {
		this.defaultTreeNode = defaultTreeNode;
	}
	
	public IdmIdentity getDefaultLeader() {
		return defaultLeader;
	}

	public void setDefaultLeader(IdmIdentity defaultLeader) {
		this.defaultLeader = defaultLeader;
	}

	public boolean isStartOfHrProcesses() {
		return startOfHrProcesses;
	}

	public void setStartOfHrProcesses(boolean startOfHrProcesses) {
		this.startOfHrProcesses = startOfHrProcesses;
	}

	public boolean isStartAutoRoleRecalculation() {
		return startAutoRoleRecalculation;
	}

	public void setStartAutoRoleRecalculation(boolean startAutoRoleRecalculation) {
		this.startAutoRoleRecalculation = startAutoRoleRecalculation;
	}
}
