package eu.bcvsolutions.idm.core.config;

import org.junit.Test;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
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
		super.convertSwagger(CoreModuleDescriptor.MODULE_ID);
	}
    
}