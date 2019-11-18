package eu.bcvsolutions.idm.rpt.api.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Generated report filter
 * 
 * @author Radek Tomiška
 *
 */
public class RptReportFilter extends DataFilter {
	
	public final static String PARAMETER_LONG_RUNNING_TASK_ID = "longRunningTaskId"; 
	//
	private DateTime from;
	private DateTime till;

	public RptReportFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public RptReportFilter(MultiValueMap<String, Object> data) {
		super(RptReportDto.class, data);
	}
	
	public DateTime getFrom() {
		return from;
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public DateTime getTill() {
		return till;
	}

	public void setTill(DateTime till) {
		this.till = till;
	}
	
	public UUID getLongRunningTaskId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_LONG_RUNNING_TASK_ID));
	}
	
	public void setLongRunningTaskId(UUID longRunningTaskId) {
		data.set(PARAMETER_LONG_RUNNING_TASK_ID, longRunningTaskId);
	}
}
