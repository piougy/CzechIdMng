package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for {@link SysProvisioningBreakConfig}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SysProvisioningBreakConfigFilter implements BaseFilter {

	private Integer warningLimit;
	private Integer disableLimit;
	private Long period;
	private ProvisioningEventType operationType;
	private UUID systemId;
	private boolean includeGlobalConfig = false;

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

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public boolean isIncludeGlobalConfig() {
		return includeGlobalConfig;
	}

	public void setIncludeGlobalConfig(boolean includeGlobalConfig) {
		this.includeGlobalConfig = includeGlobalConfig;
	}
}
