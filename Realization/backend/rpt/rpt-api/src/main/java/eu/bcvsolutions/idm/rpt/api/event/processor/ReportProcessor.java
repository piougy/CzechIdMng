package eu.bcvsolutions.idm.rpt.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Report processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface ReportProcessor extends EntityEventProcessor<RptReportDto> {
	
}
