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

/**
 * Uniform password definition and connection to {@link SysSystem}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Entity
@Table(name = "acc_uniform_password_system", indexes = {
		@Index(name = "ux_acc_uniform_pass_id_sys_id", columnList = "system_id,uniform_password_id", unique = true),
		@Index(name = "idx_sys_system_id", columnList = "system_id"),
		@Index(name = "idx_acc_uniform_password_id", columnList = "uniform_password_id") })
public class AccUniformPasswordSystem extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "uniform_password_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccUniformPassword uniformPassword;

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public AccUniformPassword getUniformPassword() {
		return uniformPassword;
	}

	public void setUniformPassword(AccUniformPassword uniformPassword) {
		this.uniformPassword = uniformPassword;
	}
	
}
