package eu.bcvsolutions.idm.vs.repository.filter;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.vs.domain.VsRequestEventType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;

/**
 * Filter for vs request
 * 
 * @author Svanda
 *
 */

public class RequestFilter extends QuickFilter {

	private String uid;
	private UUID systemId;
	private String connectorKey;
	private VsRequestEventType operationType;
	private VsRequestState state;
	private Boolean unfinished;
	
	public void setUid(String uidValue) {
		this.uid = uidValue;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}

	public VsRequestEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsRequestEventType operationType) {
		this.operationType = operationType;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}

	public Boolean getUnfinished() {
		return unfinished;
	}

	public void setUnfinished(Boolean unfinished) {
		this.unfinished = unfinished;
	}
}
