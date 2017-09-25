package eu.bcvsolutions.idm.acc.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
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
	private ProvisioningOperationType operationType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = IdmNotificationTemplateDto.class)
	private UUID emailTemplateWarning;
	@Embedded(dtoClass = IdmNotificationTemplateDto.class)
	private UUID emailTemplateDisabled;
	private List<UUID> recipients;

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

	public List<UUID> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<UUID> recipients) {
		this.recipients = recipients;
	}

}
