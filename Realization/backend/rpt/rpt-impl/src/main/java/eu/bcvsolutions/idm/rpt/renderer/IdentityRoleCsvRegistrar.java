package eu.bcvsolutions.idm.rpt.renderer;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.rpt.api.renderer.AbstractRendererRegistrar;
import eu.bcvsolutions.idm.rpt.executor.IdentityRoleReportExecutor;

/**
 * Register csv renderer to report
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdentityRoleCsvRegistrar extends AbstractRendererRegistrar {

	public IdentityRoleCsvRegistrar() {
		super(IdentityRoleReportExecutor.REPORT_NAME, IdentityRoleCsvRenderer.RENDERER_NAME);
	}
}
