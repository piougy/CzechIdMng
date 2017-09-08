package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for {@link SysProvisioningRequest}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ProvisioningRequestFilter implements BaseFilter {

	private UUID operationId;
	private UUID batchId;

	public UUID getOperation() {
		return operationId;
	}

	public void setOperationId(UUID operation) {
		this.operationId = operation;
	}

	public UUID getBatchId() {
		return batchId;
	}

	public void setBatch(UUID batch) {
		this.batchId = batch;
	}

}
