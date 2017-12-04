package eu.bcvsolutions.idm.rpt.api.renderer;

import java.io.InputStream;

import org.springframework.http.MediaType;

import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Render report into json
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractJsonRenderer extends AbstractReportRenderer {
	
	public static final String RENDERER_EXTENSION = "json";
	
	@Override
	public InputStream render(RptReportDto report) {
		return getReportData(report);
	}

	@Override
	public MediaType getFormat() {
		return MediaType.APPLICATION_JSON_UTF8;
	}
	
	@Override
	public String getExtension() {
		return RENDERER_EXTENSION;
	}
}
