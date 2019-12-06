package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete contracts integration test
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	@Autowired private IdmIdentityContractService service;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityContractDto> dtos = createBulk(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmIdentityContract.class, IdentityContractDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(dtos);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmIdentityContractDto> dtos = createBulk(5);
		List<IdmIdentityContractDto> otherDtos = createBulk(2);
		
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(dtos.get(0).getIdentity());

		List<IdmIdentityContractDto> checkDtos = service.find(filter, null).getContent();
		Assert.assertEquals(6, checkDtos.size()); // with primary

		IdmBulkActionDto bulkAction = findBulkAction(IdmIdentityContract.class, IdentityContractDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 6l, null, null);
	
		for (IdmIdentityContractDto dto : dtos) {
			Assert.assertNull(service.get(dto));
		}
		
		Assert.assertNotNull(service.get(otherDtos.get(0)));
		Assert.assertNotNull(service.get(otherDtos.get(1)));
	}
	
	private List<IdmIdentityContractDto> createBulk(int count) {
		List<IdmIdentityContractDto> results = new ArrayList<>();
		//
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		for (int i = 0; i < count; i++) {
			results.add(getHelper().createIdentityContact(owner));
		}
		//
		return results;
	}
}
