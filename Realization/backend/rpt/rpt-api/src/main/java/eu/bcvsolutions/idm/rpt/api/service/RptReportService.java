package eu.bcvsolutions.idm.rpt.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;

/**
 * CRUD for generated reports
 * 
 * @author Radek Tomiška
 *
 */
public interface RptReportService extends
	ReadWriteDtoService<RptReportDto, RptReportFilter>,
	AuthorizableService<RptReportDto> {

}
