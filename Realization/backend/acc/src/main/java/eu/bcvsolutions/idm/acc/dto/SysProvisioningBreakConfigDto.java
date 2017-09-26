package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;

/**
 * DTO for {@link SysProvisioningBreakConfig}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "provisioningBreakConfigs")
public class SysProvisioningBreakConfigDto extends AbstractDto {

	private static final long serialVersionUID = 4355611031662825999L;

	private Integer warningLimit;
	private Integer disableLimit;
	private Long period;
	private boolean operationDisabled = false;
	private boolean disabled;
	private ProvisioningEventType operationType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = IdmNotificationTemplateDto.class)
	private UUID emailTemplateWarning;
	@Embedded(dtoClass = IdmNotificationTemplateDto.class)
	private UUID emailTemplateDisabled;

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

	/**
	 * Get period in MIN
	 * 
	 * @return
	 */
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

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public UUID getEmailTemplateWarning() {
		return emailTemplateWarning;
	}

	public void setEmailTemplateWarning(UUID emailTemplateWarning) {
		this.emailTemplateWarning = emailTemplateWarning;
	}

	public UUID getEmailTemplateDisabled() {
		return emailTemplateDisabled;
	}

	public void setEmailTemplateDisabled(UUID emailTemplateDisabled) {
		this.emailTemplateDisabled = emailTemplateDisabled;
	}

	/**
	 * Return period to another time unit.
	 * 
	 * @param timeUnit
	 * @return
	 */
	public Long getPeriod(TimeUnit timeUnit) {
		if (getPeriod() == null) {
			return null;
		}
		if (timeUnit == TimeUnit.DAYS) {
			return TimeUnit.DAYS.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.HOURS) {
			return TimeUnit.HOURS.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.MICROSECONDS) {
			return TimeUnit.MICROSECONDS.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.MILLISECONDS) {
			return TimeUnit.MILLISECONDS.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.MINUTES) {
			return TimeUnit.MINUTES.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.NANOSECONDS) {
			return TimeUnit.NANOSECONDS.convert(getPeriod(), TimeUnit.MINUTES);
		} else if (timeUnit == TimeUnit.SECONDS) {
			return TimeUnit.SECONDS.convert(getPeriod(), TimeUnit.MINUTES);
		} else {
			return getPeriod();
		}
	}
}
