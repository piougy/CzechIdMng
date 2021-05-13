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

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete contracts integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
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
			Assert.assertNull(contractService.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmIdentityContractDto> dtos = createBulk(5);
		List<IdmIdentityContractDto> otherDtos = createBulk(2);
		
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(dtos.get(0).getIdentity());

		List<IdmIdentityContractDto> checkDtos = contractService.find(filter, null).getContent();
		Assert.assertEquals(6, checkDtos.size()); // with primary

		IdmBulkActionDto bulkAction = findBulkAction(IdmIdentityContract.class, IdentityContractDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 6l, null, null);
	
		for (IdmIdentityContractDto dto : dtos) {
			Assert.assertNull(contractService.get(dto));
		}
		
		Assert.assertNotNull(contractService.get(otherDtos.get(0)));
		Assert.assertNotNull(contractService.get(otherDtos.get(1)));
	}
	
	@Test
	public void testForceDeleteAsync() {
		// create identity
		String description = getHelper().createName();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setDescription(description);
		IdmIdentityDto identityOne = identityService.save(identity);
		// create role
		IdmRoleDto role = getHelper().createRole();
		// create automatic roles - by tree and by attribute too
		IdmAutomaticRoleAttributeDto automaticRoleOne = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(automaticRoleOne.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.description.getName(), null, description);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identityOne);
		contract.setWorkPosition(treeNode.getId());
		contractService.save(contract);
		getHelper().createRoleTreeNode(role, treeNode, false);
		// create manuallyAssigned roles
		getHelper().createIdentityRole(identityOne, role);
		//
		Assert.assertEquals(3, identityRoleService.findAllByIdentity(identityOne.getId()).size());
		// remove role async
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			//
			// delete by bulk action
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentityContract.class, IdentityContractDeleteBulkAction.NAME);
			bulkAction.setIdentifiers(Sets.newHashSet(contract.getId()));
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			checkResultLrt(processAction, 1l, 0l, 0l);
			//
			getHelper().waitForResult(res -> {
				return contractService.get(contract) != null;
			});
			//
			Assert.assertTrue(identityRoleService.findAllByIdentity(identityOne.getId()).isEmpty());
			Assert.assertNull(contractService.get(contract));
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
	
	private List<IdmIdentityContractDto> createBulk(int count) {
		List<IdmIdentityContractDto> results = new ArrayList<>();
		//
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		for (int i = 0; i < count; i++) {
			results.add(getHelper().createContract(owner));
		}
		//
		return results;
	}
}
