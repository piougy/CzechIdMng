package eu.bcvsolutions.idm.rpt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import eu.bcvsolutions.idm.rpt.api.executor.ReportExecutor;
import eu.bcvsolutions.idm.rpt.api.renderer.ReportRenderer;

/**
 * Module services configuration
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
@EnablePluginRegistries({ ReportExecutor.class, ReportRenderer.class })
public class RptServiceConfiguration {
	
	// TODO: service + manager + plugins
	
}
