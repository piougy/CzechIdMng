package eu.bcvsolutions.idm.rpt.api.renderer;

import org.springframework.util.Assert;

/**
 * Common renderer registrar - register given renderers to given report
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractRendererRegistrar implements RendererRegistrar {

	private final String reportName;
	private final String[] rendererNames;
	
	/**
	 * 
	 * @param reportName
	 * @param rendererName
	 */
	public AbstractRendererRegistrar(String reportName, String... rendererName) {
		Assert.hasLength(reportName);
		Assert.notNull(rendererName);
		//
		this.reportName = reportName;
		this.rendererNames = rendererName;
	}
	
	@Override
	public String[] register(String reportName) {
		if (this.reportName.equals(reportName)) {
			return rendererNames;
		}
		return new String[]{};
	}
}
