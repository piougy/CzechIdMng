package eu.bcvsolutions.idm.rpt.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportRendererDto;

/**
 * Generates and renders reports
 * 
 * @author Radek Tomiška
 *
 */
public interface ReportManager {
	
	/**
	 * Generate report:
	 * - Creates new report by given name and filter.
	 * - or generates data for existing report (update) and actual filter.
	 * Report is persisted (create report or filter is updated) and published to processors for generate.
	 * 
	 * @param report created report.
	 * @return
	 */
	RptReportDto generate(RptReportDto report);
	
	/**
	 * Generate given report. Report has to be persisted before.
	 * This method generates persisted report only
	 * 
	 * @param event
	 */
	void generate(EntityEvent<RptReportDto> event);
	
	/**
	 * Render report by given renderer
	 * 
	 * @param report
	 * @return
	 */
	RptRenderedReportDto render(RptReportDto report, String rendererName);
	
	/**
	 * Returns registered report executors
	 * 
	 * @return
	 */
	List<RptReportExecutorDto> getExecutors();
	
	/**
	 * Returns renderers for given report
	 * 
	 * @param reportName
	 * @return
	 */
	List<RptReportRendererDto> getRenderers(String reportName);
}
