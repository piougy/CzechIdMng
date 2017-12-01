package eu.bcvsolutions.idm.rpt.api.dto;

import java.io.InputStream;

/**
 * Rendered report
 * 
 * @author Radek Tomiška
 *
 */
public class RptRenderedReportDto {

	private RptReportDto inputReport;
	private RptReportRendererDto renderer;
	private InputStream renderedReport;

	public RptReportDto getInputReport() {
		return inputReport;
	}

	public void setInputReport(RptReportDto inputReport) {
		this.inputReport = inputReport;
	}
	
	public void setRenderer(RptReportRendererDto renderer) {
		this.renderer = renderer;
	}
	
	public RptReportRendererDto getRenderer() {
		return renderer;
	}
	
	public InputStream getRenderedReport() {
		return renderedReport;
	}
	
	public void setRenderedReport(InputStream renderedReport) {
		this.renderedReport = renderedReport;
	}
}
