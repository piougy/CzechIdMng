package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Entity that store information about recipient for
 * {@link SysProvisioningBreakConfig}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "sys_provisioning_break_recipient", indexes = {
		@Index(name = "idx_sys_prov_br_recip_role_id", columnList = "role_id"),
		@Index(name = "idx_sys_prov_br_recip_identity_id", columnList = "identity_id"),
		@Index(name = "idx_sys_prov_br_break_id", columnList = "break_config_id")
})
public class SysProvisioningBreakRecipient extends AbstractEntity {

	private static final long serialVersionUID = 4226278248262132987L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "break_config_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysProvisioningBreakConfig breakConfig;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmIdentity identity;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRole role;

	public SysProvisioningBreakConfig getBreakConfig() {
		return breakConfig;
	}

	public void setBreakConfig(SysProvisioningBreakConfig breakConfig) {
		this.breakConfig = breakConfig;
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
