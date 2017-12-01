package eu.bcvsolutions.idm.rpt.api.dto.filter;

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

	public RptReportFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public RptReportFilter(MultiValueMap<String, Object> data) {
		super(RptReportDto.class, data);
	}
}
