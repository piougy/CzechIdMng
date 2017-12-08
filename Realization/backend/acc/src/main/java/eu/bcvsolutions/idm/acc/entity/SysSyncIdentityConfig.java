package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * <i>SysSyncContractConfig</i> is responsible for keep specific informations about
 * identity synchronization configuration
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_identity_config", indexes = {
		@Index(name = "idx_sys_s_iden_conf_role", columnList = "default_role_id")
		})
public class SysSyncIdentityConfig extends SysSyncConfig{

	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "default_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole defaultRole;

	public IdmRole getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(IdmRole defaultRole) {
		this.defaultRole = defaultRole;
	}
	
}
