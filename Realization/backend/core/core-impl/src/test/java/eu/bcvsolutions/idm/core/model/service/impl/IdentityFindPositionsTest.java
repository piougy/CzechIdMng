package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultContractByManagerFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultSubordinatesFilter;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for identity service find managers and role.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Transactional
public class IdentityFindPositionsTest extends AbstractIntegrationTest{

	@Autowired
	private IdmIdentityService identityService;	
	@Autowired
	private IdmIdentityContractService identityContractService;	
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired 
	private FilterManager filterManager;
	
	@Test
	public void testFindUser() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		
		IdmIdentityDto foundIdentity = this.identityService.get(identity.getId());
		
		assertEquals(identity, foundIdentity);
	}
	
	@Test
	public void testFindGuarantee() {
		IdmIdentityDto user = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto quarantee1 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto quarantee2 = getHelper().createIdentity((GuardedString) null);
		
		createIdentityContract(user, quarantee1, null);
		
		createIdentityContract(user, quarantee2, null);
		
		List<IdmIdentityDto> result = identityService.findAllManagers(user.getId(), null);
		
		assertEquals(2, result.size());
	}
	
	@Test
	public void testFindManagers() {
		FilterBuilder<IdmIdentity, DataFilter> filterBuilderSubordinates = filterManager.getBuilder(
				IdmIdentity.class, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR);
		FilterBuilder<IdmIdentity, DataFilter> filterBuilderManagers = filterManager.getBuilder(
				IdmIdentity.class, IdmIdentityFilter.PARAMETER_MANAGERS_FOR);
		FilterBuilder<IdmIdentityContract, DataFilter> filterBuilderContractByManager = filterManager.getBuilder(
				IdmIdentityContract.class, IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR);
		Assert.assertEquals(DefaultManagersFilter.FILTER_NAME, filterBuilderManagers.getId());
		Assert.assertEquals(DefaultSubordinatesFilter.FILTER_NAME, filterBuilderSubordinates.getId());
		Assert.assertEquals(DefaultContractByManagerFilter.FILTER_NAME, filterBuilderContractByManager.getId());
		Assert.assertFalse(filterBuilderSubordinates.isDisabled());
		Assert.assertFalse(filterBuilderManagers.isDisabled());
		Assert.assertFalse(filterBuilderContractByManager.isDisabled());
		//
		IdmIdentityDto user = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto user2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto user3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto user4 = getHelper().createIdentity((GuardedString) null);
		
		IdmTreeTypeDto treeTypeFirst = getHelper().createTreeType();
		IdmTreeTypeDto treeTypeSecond = getHelper().createTreeType();
		
		// create root for second type
		getHelper().createTreeNode(treeTypeSecond, null);
		
		// create root for first type
		IdmTreeNodeDto nodeRoot = getHelper().createTreeNode(treeTypeFirst, null);
		
		// create one for first type
		IdmTreeNodeDto nodeOne = getHelper().createTreeNode(treeTypeFirst, nodeRoot);
		
		// create two for first type
		IdmTreeNodeDto nodeTwo = getHelper().createTreeNode(treeTypeFirst, nodeOne);
		
		createIdentityContract(user, null, nodeRoot);
		createIdentityContract(user2, null, nodeOne);
		createIdentityContract(user3, null, nodeOne);
		createIdentityContract(user4, null, nodeTwo);
		// createIdentityContract(user, manager3, null);
		
		List<IdmIdentityDto> managersList = identityService.findAllManagers(user3.getId(), treeTypeFirst.getId());
		assertEquals(1, managersList.size());
		
		IdmIdentityDto manager = managersList.get(0);
		assertEquals(user.getId(), manager.getId());
		
		managersList = identityService.findAllManagers(user4.getId(), treeTypeFirst.getId());
		assertEquals(2, managersList.size());
		
		managersList = identityService.findAllManagers(user.getId(), treeTypeFirst.getId());
		assertEquals(1, managersList.size());
		
		createIdentityContract(user, null, nodeTwo);
		managersList = identityService.findAllManagers(user.getId(), treeTypeFirst.getId());
		assertEquals(2, managersList.size());
		
		List<IdmIdentityDto> managersListSec = identityService.findAllManagers(user.getId(), treeTypeSecond.getId());
		
		// user with superAdminRole
		assertEquals(1, managersListSec.size());
	}
	
	@Test
	public void testManagerNotFound() {
		IdmIdentityDto user = getHelper().createIdentity((GuardedString) null);
		
		List<IdmIdentityDto> result = identityService.findAllManagers(user.getId(), null);
		
		assertEquals(1, result.size());
		
		IdmIdentityDto admin = result.get(0);
		
		assertNotNull(admin);
	}
	
	private IdmIdentityContractDto createIdentityContract(IdmIdentityDto user, IdmIdentityDto guarantee, IdmTreeNodeDto node) {
		IdmIdentityContractDto position = new IdmIdentityContractDto();
		position.setIdentity(user.getId());
		position.setWorkPosition(node == null ? null : node.getId());
		
		position = identityContractService.save(position);
		
		if (guarantee != null) {
			contractGuaranteeService.save(new IdmContractGuaranteeDto(position.getId(), guarantee.getId()));
		}
		
		return position;
	}
}
