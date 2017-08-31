package eu.bcvsolutions.idm.vs.repository.filter;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;

/**
 * Filter for vs request
 * 
 * @author Svanda
 *
 */

public class VsRequestFilter extends QuickFilter {

	private String uid;
	private UUID systemId;
	private String connectorKey;
	private VsOperationType operationType;
	private VsRequestState state;
	
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

	public VsOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsOperationType operationType) {
		this.operationType = operationType;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}
}
