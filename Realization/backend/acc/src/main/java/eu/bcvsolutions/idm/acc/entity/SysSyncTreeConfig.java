package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * <i>SysSyncTreeConfig</i> is responsible for keep specific information about
 * tree synchronization configuration.
 * 
 * @author Radek Tomiška
 * @since 10.4.0 
 */
@Entity
@Table(name = "sys_sync_tree_config")
public class SysSyncTreeConfig extends SysSyncConfig{

	private static final long serialVersionUID = 1L;
	
	@Audited
	@NotNull
	@Column(name = "start_auto_role_rec", nullable = false)
	private boolean startAutoRoleRec = true;

	/**
	 * Start recalculation after end synchronization for automatic roles.
	 * 
	 * @return start
	 */
	public boolean isStartAutoRoleRec() {
		return startAutoRoleRec;
	}

	/**
	 * Start recalculation after end synchronization for automatic roles.
	 * 
	 * @param startAutoRoleRec start
	 */
	public void setStartAutoRoleRec(boolean startAutoRoleRec) {
		this.startAutoRoleRec = startAutoRoleRec;
	}
}
