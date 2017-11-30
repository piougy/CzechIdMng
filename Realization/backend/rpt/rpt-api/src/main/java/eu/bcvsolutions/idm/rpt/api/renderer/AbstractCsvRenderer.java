package eu.bcvsolutions.idm.rpt.api.renderer;

import org.springframework.http.MediaType;

/**
 * Render report into csv
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractCsvRenderer extends AbstractReportRenderer {
	
	public static final String RENDERER_EXTENSION = "csv";
	
	@Override
	public MediaType getFormat() {
		return new MediaType("text", getExtension());
	}
	
	@Override
	public String getExtension() {
		return RENDERER_EXTENSION;
	}
}
