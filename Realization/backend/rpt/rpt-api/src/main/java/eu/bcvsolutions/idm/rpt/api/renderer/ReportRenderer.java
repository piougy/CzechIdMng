package eu.bcvsolutions.idm.rpt.api.renderer;

import java.io.InputStream;

import org.springframework.http.MediaType;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Renders report to given format
 * - more renderers can be registred to one report
 * 
 * @author Radek Tomiška
 *
 */
public interface ReportRenderer extends Configurable, Plugin<String> {
	
	@Override
	default String getConfigurableType() {
		return "report-renderer";
	}
	
	/**
	 * Renders report
	 * 
	 * @param report
	 * @return rendered report (data + metadata) as input stream
	 */
	InputStream render(RptReportDto report);
	
	/**
	 * Renders report to format
	 * 
	 * @return
	 */
	MediaType getFormat();
	
	/**
	 * Returns report extension
	 * 
	 * @return
	 */
	String getExtension();
}
