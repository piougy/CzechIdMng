package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;

/**
 * Entity that store configuration for break.
 * Email recipients for break is stored in {@link SysProvisioningBreakRecipient}
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "sys_provisioning_break_config", indexes = {})
public class SysProvisioningBreakConfig extends AbstractEntity {

	private static final long serialVersionUID = 579580240598032453L;

	@Audited
	@Column(name = "warning_limit", nullable = true)
	private Integer warningLimit;

	@Audited
	@Column(name = "disable_limit", nullable = false)
	private Integer disableLimit;

	@Audited
	@Column(name = "period", nullable = false)
	private Long period;

	@Audited
	@Column(name = "operation_disabled", nullable = false)
	private boolean operationDisabled = false;

	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private ProvisioningOperationType operationType;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystem system;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "email_template_warning", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmNotificationTemplate emailTemplateWarning;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "email_template_disabled", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmNotificationTemplate emailTemplateDisabled;

	public Integer getWarningLimit() {
		return warningLimit;
	}

	public void setWarningLimit(Integer warningLimit) {
		this.warningLimit = warningLimit;
	}

	public Integer getDisableLimit() {
		return disableLimit;
	}

	public void setDisableLimit(Integer disableLimit) {
		this.disableLimit = disableLimit;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public boolean isOperationDisabled() {
		return operationDisabled;
	}

	public void setOperationDisabled(boolean operationDisabled) {
		this.operationDisabled = operationDisabled;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public ProvisioningOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningOperationType operationType) {
		this.operationType = operationType;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public IdmNotificationTemplate getEmailTemplateWarning() {
		return emailTemplateWarning;
	}

	public void setEmailTemplateWarning(IdmNotificationTemplate emailTemplateWarning) {
		this.emailTemplateWarning = emailTemplateWarning;
	}

	public IdmNotificationTemplate getEmailTemplateDisabled() {
		return emailTemplateDisabled;
	}

	public void setEmailTemplateDisabled(IdmNotificationTemplate emailTemplateDisabled) {
		this.emailTemplateDisabled = emailTemplateDisabled;
	}

}
