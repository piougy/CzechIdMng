package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
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
	private ProvisioningOperationType operationType;
	private UUID systemId;

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

	public ProvisioningOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningOperationType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

}
