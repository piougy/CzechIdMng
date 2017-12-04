package eu.bcvsolutions.idm.example.report;

import java.io.InputStream;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

/**
 * Example xlsx renderer - renders report wit identities
 * 
 * @author Radek Tomiška
 *
 */
@Component("exampleIdentityReportRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class IdentityReportRenderer 
		extends AbstractXlsxRenderer
		implements RendererRegistrar {

	@Override
	public InputStream render(RptReportDto report) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] register(String reportName) {
		if (IdentityReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[]{ getName() };
		}
		return new String[]{};
	}

}
