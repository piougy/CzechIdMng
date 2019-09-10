package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedFromFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for synchronization log
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
public class SysSyncLogFilter extends DataFilter implements ModifiedFromFilter {

	public static final String PARAMETER_SYNCHRONIZATION_CONFIG_ID = "synchronizationConfigId";
	public static final String PARAMETER_RUNNING = "running";
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_MODIFIED_FROM = "modifiedFrom";
	
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
	
	public UUID getSystemId() {
		return getParameterConverter().toUuid(data, PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		data.set(PARAMETER_SYSTEM_ID, systemId);
	}
	
	public DateTime getModifiedFrom() {
		return getParameterConverter().toDateTime(data, PARAMETER_MODIFIED_FROM);
	}

	public void setModifiedFrom(DateTime dateTime) {
		data.set(PARAMETER_MODIFIED_FROM, dateTime);
	}
}
