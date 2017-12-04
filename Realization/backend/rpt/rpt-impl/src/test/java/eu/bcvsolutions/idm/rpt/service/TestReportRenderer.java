package eu.bcvsolutions.idm.rpt.service;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.rpt.api.renderer.AbstractJsonRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

/**
 * Renders test report into json
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("json")
public class TestReportRenderer 
		extends AbstractJsonRenderer
		implements RendererRegistrar {

	public static final String RENDERER_NAME = "test-json-renderer";
	
	@Override
	public String getName() {
		return RENDERER_NAME;
	}
	
	@Override
	public String[] register(String reportName) {
		if (TestReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[]{ getName() };
		}
		return new String[]{};
	}

	
}
