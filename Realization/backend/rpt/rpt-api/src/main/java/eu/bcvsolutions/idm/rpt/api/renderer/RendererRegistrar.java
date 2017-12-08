package eu.bcvsolutions.idm.rpt.api.renderer;

/**
 * Register renderer to report
 * 
 * @author Radek Tomiška
 *
 */
public interface RendererRegistrar {

	/**
	 * Register renderers for given report ~ report executor name
	 * 
	 * @param reportName report executor name
	 * @return
	 */
	String[] register(String reportName);
}
