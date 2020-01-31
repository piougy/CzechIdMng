package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for synchronization config.
 * 
 * @author Svanda
 * @author Radek Tomiška
 * @author Ondrej Husnik
 */
public class SysSyncConfigFilter extends DataFilter {

	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_NAME = "name";
	public static final String PARAMETER_INCLUDE_LAST_LOG = "includeLastLog";
	public static final String PARAMETER_DIFFERENTIAL_SYNC = "differentialSync";
	public static final String PARAMETER_SYSTEM_MAPPING_ID = "systemMappingId";
	
	public SysSyncConfigFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSyncConfigFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSyncConfigFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(null, data, parameterConverter); // FIXME: filter used for more DTOs - abstract sync is returned
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(data, PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		data.set(PARAMETER_SYSTEM_ID, systemId);
	}

	public String getName() {
		return getParameterConverter().toString(data, PARAMETER_NAME);
	}

	public void setName(String name) {
		data.set(PARAMETER_NAME, name);
	}
	
	public Boolean getIncludeLastLog() {
		return getParameterConverter().toBoolean(data, PARAMETER_INCLUDE_LAST_LOG);
	}

	public void setIncludeLastLog(Boolean includeLastLog) {
		data.set(PARAMETER_INCLUDE_LAST_LOG, includeLastLog);
	}
	
	public Boolean getDifferentialSync() {
		return getParameterConverter().toBoolean(data, PARAMETER_DIFFERENTIAL_SYNC);
	}
	
	public void setDifferentialSync(Boolean differentialSync) {
		data.set(PARAMETER_DIFFERENTIAL_SYNC, differentialSync);
	}
	
	public UUID getSystemMappingId() {
		return getParameterConverter().toUuid(data, PARAMETER_SYSTEM_MAPPING_ID);
	}

	public void setSystemMappingId(UUID systemMappingId) {
		data.set(PARAMETER_SYSTEM_MAPPING_ID, systemMappingId);
	}

}
