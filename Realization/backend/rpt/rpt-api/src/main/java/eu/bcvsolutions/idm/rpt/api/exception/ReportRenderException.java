package eu.bcvsolutions.idm.rpt.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;

/**
 * Report render failed
 * 
 * @author Radek Tomiška
 *
 */
public class ReportRenderException extends ResultCodeException {

	private static final long serialVersionUID = 1L;
	private final String reportName;	
	
	public ReportRenderException(String reportName, Exception ex) {
		super(RptResultCode.REPORT_RENDER_FAILED, ImmutableMap.of("reportName", reportName), ex);
		this.reportName = reportName;
	}
	
	public String getReportName() {
		return reportName;
	}
}
