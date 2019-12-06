
package eu.bcvsolutions.idm.tool.config;

import org.junit.Test;

import eu.bcvsolutions.idm.tool.ToolModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractSwaggerTest;


/**
 * Static swagger generation to sources - will be used as input for swagger2Markup build
 *
 * @author BCV solutions s.r.o.
 *
 */
public class Swagger2MarkupTest extends AbstractSwaggerTest {

	@Test
	public void testConvertSwagger() throws Exception {
		super.convertSwagger(ToolModuleDescriptor.MODULE_ID);
	}

}
