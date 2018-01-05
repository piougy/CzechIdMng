package eu.bcvsolutions.idm.rpt.api.dto.filter;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Generated report filter
 * 
 * @author Radek Tomiška
 *
 */
public class RptReportFilter extends DataFilter {
	
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
}
