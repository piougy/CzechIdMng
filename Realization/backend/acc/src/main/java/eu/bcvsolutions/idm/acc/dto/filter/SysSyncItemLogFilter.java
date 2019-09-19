package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for synchronization item log
 * 
 * @author Svanda
 *
 */
public class SysSyncItemLogFilter extends DataFilter {

	private UUID syncActionLogId;
	private String displayName; //Search with like
	private UUID systemId;
	
	public SysSyncItemLogFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSyncItemLogFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSyncItemLogFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSyncLogDto.class, data, parameterConverter);
	}
	

	public UUID getSyncActionLogId() {
		return syncActionLogId;
	}

	public void setSyncActionLogId(UUID syncActionLogId) {
		this.syncActionLogId = syncActionLogId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
}
