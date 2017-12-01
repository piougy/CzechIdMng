package eu.bcvsolutions.idm.rpt.renderer;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractJsonRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

/**
 * Renders given data into json
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description(AbstractJsonRenderer.RENDERER_EXTENSION)
public class DefaultJsonRenderer 
		extends AbstractJsonRenderer
		implements RendererRegistrar {
	
	public static final String RENDERER_NAME = "default-json-renderer";
	
	@Override
	public String getName() {
		return RENDERER_NAME;
	}
	
	@Override
	public String getModule() {
		return RptModuleDescriptor.MODULE_ID;
	}

	@Override
	public String[] register(String reportName) {
		// supports all reports
		return new String[]{ RENDERER_NAME };
	}
}
