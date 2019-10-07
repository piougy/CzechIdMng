package eu.bcvsolutions.idm.rpt.api.dto.filter;

import java.time.ZonedDateTime;
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
	
	private ZonedDateTime from;
	private ZonedDateTime till;

	public RptReportFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public RptReportFilter(MultiValueMap<String, Object> data) {
		super(RptReportDto.class, data);
	}
	
	public ZonedDateTime getFrom() {
		return from;
	}

	public void setFrom(ZonedDateTime from) {
		this.from = from;
	}

	public ZonedDateTime getTill() {
		return till;
	}

	public void setTill(ZonedDateTime till) {
		this.till = till;
	}
}
