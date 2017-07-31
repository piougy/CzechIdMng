package eu.bcvsolutions.idm.example.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;
import io.swagger.annotations.Api;

/**
 * RESTful example product endpoint
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/example-products")
@Api(
		value = ExampleProductController.TAG, 
		description = "Example products", 
		tags = { ExampleProductController.TAG })
public class ExampleProductController extends DefaultReadWriteDtoController<ExampleProductDto, ExampleProductFilter> {

	protected static final String TAG = "Example products";
	
	@Autowired
	public ExampleProductController(ExampleProductService service) {
		super(service);
	}
}
