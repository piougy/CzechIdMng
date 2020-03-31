package eu.bcvsolutions.idm.vs.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;

/**
 * Filter for vs request
 * 
 * @author Svanda
 *
 */
public class VsRequestFilter extends DataFilter implements ExternalIdentifiableFilter {

	private String uid;
	private UUID systemId;
	private String connectorKey;
	private VsOperationType operationType;
	private VsRequestState state;
	private ZonedDateTime createdAfter; // TODO: createdFrom alias
	private ZonedDateTime createdBefore; // TODO: createdTill alias
	private Boolean onlyArchived;
	private UUID roleRequestId;
	private ZonedDateTime modifiedAfter; // TODO: modifiedFrom alias
	private ZonedDateTime modifiedBefore; // TODO: modifiedTill alias
	
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

	public ZonedDateTime getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(ZonedDateTime createdAfter) {
		this.createdAfter = createdAfter;
	}

	public ZonedDateTime getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(ZonedDateTime createdBefore) {
		this.createdBefore = createdBefore;
	}
	
	public ZonedDateTime getModifiedAfter() {
		return modifiedAfter;
	}

	public void setModifiedAfter(ZonedDateTime modifiedAfter) {
		this.modifiedAfter = modifiedAfter;
	}

	public ZonedDateTime getModifiedBefore() {
		return modifiedBefore;
	}

	public void setModifiedBefore(ZonedDateTime modifiedBefore) {
		this.modifiedBefore = modifiedBefore;
	}

	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}
}
