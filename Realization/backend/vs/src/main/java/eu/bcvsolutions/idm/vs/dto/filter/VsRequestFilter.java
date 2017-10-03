package eu.bcvsolutions.idm.vs.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;

/**
 * Filter for vs request
 * 
 * @author Svanda
 *
 */
public class VsRequestFilter extends DataFilter {

	private String uid;
	private UUID systemId;
	private String connectorKey;
	private VsOperationType operationType;
	private VsRequestState state;
	private DateTime createdAfter;
	private DateTime createdBefore;
	private Boolean onlyArchived;
	
	public VsRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public VsRequestFilter(MultiValueMap<String, Object> data) {
		super(VsRequestDto.class, data);
	}
	
	public Boolean getOnlyArchived() {
		return onlyArchived;
	}

	public void setOnlyArchived(Boolean onlyArchived) {
		this.onlyArchived = onlyArchived;
	}

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

	public DateTime getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(DateTime createdAfter) {
		this.createdAfter = createdAfter;
	}

	public DateTime getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(DateTime createdBefore) {
		this.createdBefore = createdBefore;
	}
}
