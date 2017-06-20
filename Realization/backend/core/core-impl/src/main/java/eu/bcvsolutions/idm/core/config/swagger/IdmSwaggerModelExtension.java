package eu.bcvsolutions.idm.core.config.swagger;

import io.github.swagger2markup.spi.SwaggerModelExtension;
import io.swagger.models.Swagger;

/**
 * Custom static swagger properties
 * 
 * @author Radek Tomiška
 *
 */
public class IdmSwaggerModelExtension extends SwaggerModelExtension {

	public void apply(Swagger swagger) {
		// TODO: doesn't work for now ... configure SPI properly
		
//		swagger.setHost("http://demo.czechidm.com");
//		swagger.basePath(BaseController.BASE_PATH);
//
//		Map<String, Path> paths = swagger.getPaths();
//		paths.remove(BaseController.BASE_PATH);
//		swagger.setPaths(paths);
	}
}