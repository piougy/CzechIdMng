package eu.bcvsolutions.idm.acc.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * ProvisioningOperationBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationBuilderUnitTest extends AbstractUnitTest {

	@Test
	public void testBuildEmpty() {
		SysProvisioningOperation.Builder builder = new SysProvisioningOperation.Builder();
		
		assertNotNull(builder.build());
		assertNull(builder.build().getOperationType());
	}
	
	@Test
	public void testBuildWithChange() {
		SysProvisioningOperation.Builder builder = new SysProvisioningOperation.Builder();
		
		builder.setOperationType(ProvisioningEventType.CREATE);
		ProvisioningOperation one = builder.build();
		assertEquals(ProvisioningEventType.CREATE, one.getOperationType());
		
		builder.setOperationType(ProvisioningEventType.UPDATE);
		ProvisioningOperation two = builder.build();
		assertEquals(ProvisioningEventType.CREATE, one.getOperationType());
		assertEquals(ProvisioningEventType.UPDATE, two.getOperationType());
	}
}
