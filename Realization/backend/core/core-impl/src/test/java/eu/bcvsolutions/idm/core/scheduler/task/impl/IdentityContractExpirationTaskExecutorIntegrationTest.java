package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test expired contract task executor
 * - business roles are included
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private LongRunningTaskManager lrtManager;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Test
	public void testExpiredRole() {
		getHelper().disable(IdentityContractEndProcessor.class);
		//
		try {
			IdmIdentityDto identity = getHelper().createIdentity();
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			//
			// normal and business role
			IdmRoleDto roleOne = getHelper().createRole();
			IdmRoleDto roleRoot = getHelper().createRole();
			IdmRoleDto roleSub = getHelper().createRole();
			getHelper().createRoleComposition(roleRoot, roleSub);
			//
			// assign roles
			getHelper().assignRoles(contract, roleRoot, roleOne);
			//
			// expire contract
			contract.setValidTill(LocalDate.now().minusDays(2));
			contract = getHelper().getService(IdmIdentityContractService.class).save(contract);
			//
			// test after create before lrt is executed
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setIdentityContractId(contract.getId());
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.find(filter, null).getContent();
			//
			Assert.assertEquals(3, assignedRoles.size());
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleOne.getId())));
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleRoot.getId())));
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleSub.getId())));
			//
			IdentityContractExpirationTaskExecutor lrt = new IdentityContractExpirationTaskExecutor();
			lrt.init(null);
			lrtManager.executeSync(lrt);
			//
			assignedRoles = identityRoleService.find(filter, null).getContent();
			//
			Assert.assertTrue(assignedRoles.isEmpty());
		} finally {
			getHelper().enable(IdentityContractEndProcessor.class);
		}
	}
}
