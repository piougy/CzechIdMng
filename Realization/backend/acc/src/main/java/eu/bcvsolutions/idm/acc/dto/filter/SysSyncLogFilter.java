package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for synchronization log
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
public class SysSyncLogFilter extends DataFilter {

	public static final String PARAMETER_SYNCHRONIZATION_CONFIG_ID = "synchronizationConfigId";
	public static final String PARAMETER_RUNNING = "running";
	
	public SysSyncLogFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSyncLogFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSyncLogFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSyncLogDto.class, data, parameterConverter);
	}
	
	public UUID getSynchronizationConfigId() {
		return getParameterConverter().toUuid(data, PARAMETER_SYNCHRONIZATION_CONFIG_ID);
	}

	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		data.set(PARAMETER_SYNCHRONIZATION_CONFIG_ID, synchronizationConfigId);
	}

	public Boolean getRunning() {
		return getParameterConverter().toBoolean(data, PARAMETER_RUNNING);
	}

	public void setRunning(Boolean running) {
		data.set(PARAMETER_RUNNING, running);
	}
}
