package eu.bcvsolutions.idm.core.model.entity;

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

/**
 * Role guarantee by role
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role_guarantee_role", indexes = {
				@Index(name = "idx_idm_role_g_r_role", columnList = "role_id"),
				@Index(name = "idx_idm_role_g_r_g_role", columnList = "guarantee_role_id")} )
public class IdmRoleGuaranteeRole extends AbstractEntity {

	private static final long serialVersionUID = 6106304497345109366L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role; // owner
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "guarantee_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole guaranteeRole; // guarantee as role

	/**
	 * Role owner
	 * 
	 * @return
	 */
	public IdmRole getRole() {
		return role;
	}

	/**
	 * Role owner
	 * 
	 * @param role
	 */
	public void setRole(IdmRole role) {
		this.role = role;
	}
	
	/**
	 * Guarantee as role
	 * 
	 * @return
	 */
	public IdmRole getGuaranteeRole() {
		return guaranteeRole;
	}
	
	/**
	 * Guarantee as role
	 * 
	 * @param guaranteeRole
	 */
	public void setGuaranteeRole(IdmRole guaranteeRole) {
		this.guaranteeRole = guaranteeRole;
	}
}
