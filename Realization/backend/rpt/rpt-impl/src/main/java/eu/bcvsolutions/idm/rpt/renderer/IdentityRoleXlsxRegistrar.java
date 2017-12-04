package eu.bcvsolutions.idm.rpt.renderer;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.rpt.api.renderer.AbstractRendererRegistrar;
import eu.bcvsolutions.idm.rpt.executor.IdentityRoleReportExecutor;

/**
 * Register xlsx renderer to report
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdentityRoleXlsxRegistrar extends AbstractRendererRegistrar {

	public IdentityRoleXlsxRegistrar() {
		super(IdentityRoleReportExecutor.REPORT_NAME, IdentityRoleXlsxRenderer.RENDERER_NAME);
	}
}
