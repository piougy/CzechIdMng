package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
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
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_FROM = "from"; // created from
	public static final String PARAMETER_TILL = "till"; // created till
	
	public SysSyncLogFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSyncLogFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSyncLogFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSyncLogDto.class, data, parameterConverter);
	}
	
	public DateTime getFrom() {
		return getParameterConverter().toDateTime(data, PARAMETER_FROM);
	}

	public void setFrom(DateTime from) {
		data.set(PARAMETER_FROM, from);
	}

	public DateTime getTill() {
		return getParameterConverter().toDateTime(data, PARAMETER_TILL);
	}

	public void setTill(DateTime till) {
		data.set(PARAMETER_TILL, till);
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
}
