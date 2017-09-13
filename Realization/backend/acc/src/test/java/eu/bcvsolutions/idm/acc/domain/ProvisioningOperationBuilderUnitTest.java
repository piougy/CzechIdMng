package eu.bcvsolutions.idm.acc.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * ProvisioningOperationBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationBuilderUnitTest extends AbstractVerifiableUnitTest {

	@Test
	public void testBuildEmpty() {
		SysProvisioningOperation.Builder builder = new SysProvisioningOperation.Builder();
		
		assertNotNull(builder.build());
		assertNull(builder.build().getOperationType());
	}
	
	@Test
	public void testBuildWithChange() {
		SysProvisioningOperationDto.Builder builder = new SysProvisioningOperationDto.Builder();
		
		builder.setOperationType(ProvisioningEventType.CREATE);
		ProvisioningOperation one = builder.build();
		assertEquals(ProvisioningEventType.CREATE, one.getOperationType());
		
		builder.setOperationType(ProvisioningEventType.UPDATE);
		ProvisioningOperation two = builder.build();
		assertEquals(ProvisioningEventType.CREATE, one.getOperationType());
		assertEquals(ProvisioningEventType.UPDATE, two.getOperationType());
	}
}
