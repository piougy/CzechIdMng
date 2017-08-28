package eu.bcvsolutions.idm.vs.config;

import org.junit.Test;

import eu.bcvsolutions.idm.test.api.AbstractSwaggerTest;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;


/**
 * Static swagger generation to sources - will be used as input for swagger2Markup build
 * 
 * @author Radek Tomiška
 *
 */
public class Swagger2MarkupTest extends AbstractSwaggerTest {
	
	@Test
	public void testConvertSwagger() throws Exception {
		super.convertSwagger(VirtualSystemModuleDescriptor.MODULE_ID);
	}
    
}