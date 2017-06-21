package eu.bcvsolutions.idm.example.config;

import org.junit.Test;

import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractSwaggerTest;


/**
 * Static swagger generation to sources - will be used as input for swagger2Markup build
 * 
 * @author Radek Tomiška
 *
 */
public class Swagger2MarkupTest extends AbstractSwaggerTest {
	
	@Test
	public void testConvertSwagger() throws Exception {
		super.convertSwagger(ExampleModuleDescriptor.MODULE_ID);
	}
    
}