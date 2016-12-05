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
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role could assign account on target system (account template).
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_role_system", indexes = {
		@Index(name = "idx_sys_role_system_system_id", columnList = "system_id"),
		@Index(name = "idx_sys_role_system_role_id", columnList = "role_id") })
public class SysRoleSystem extends AbstractEntity {

	private static final long serialVersionUID = -7589083183676265957L;

	@NotNull
	@Audited()
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRole role;

	@NotNull
	@Audited()
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystem system;

	@NotNull
	@Audited()
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_handling_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemEntityHandling systemEntityHandling;

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public SysSystemEntityHandling getSystemEntityHandling() {
		return systemEntityHandling;
	}

	public void setSystemEntityHandling(SysSystemEntityHandling systemEntityHandling) {
		this.systemEntityHandling = systemEntityHandling;
	}

}
