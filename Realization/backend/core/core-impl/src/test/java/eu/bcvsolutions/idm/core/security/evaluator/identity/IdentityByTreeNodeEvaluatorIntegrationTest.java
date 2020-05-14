package eu.bcvsolutions.idm.core.security.evaluator.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for {@link IdentityByTreeNodeEvaluator}
 *
 * @author Ondrej Kopr
 * @since 10.3.0
 *
 */
public class IdentityByTreeNodeEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityService identityService;

	@Test
	public void testOneContract() {
		IdmTreeNodeDto position = getHelper().createTreeNode();
		IdmRoleDto role = prepareRoleWithEvaluator(position);
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position);

		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testThreeContract() {
		IdmTreeNodeDto position = getHelper().createTreeNode();
		IdmTreeNodeDto positionTwo = getHelper().createTreeNode();
		IdmRoleDto role = prepareRoleWithEvaluator(position);
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position);

		// Two different contract
		getHelper().createIdentityContact(identity, positionTwo);
		getHelper().createIdentityContact(identity, null);

		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testSubpositionOnContract() {
		IdmTreeNodeDto parent = getHelper().createTreeNode();
		IdmTreeNodeDto parentTwo = getHelper().createTreeNode(getHelper().createName(), parent);
		IdmTreeNodeDto position = getHelper().createTreeNode(getHelper().createName(), parentTwo);

		IdmRoleDto role = prepareRoleWithEvaluator(parent); // Evaluator is on parent
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position); // Position is sub child


		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testSubpositionContract() {
		IdmTreeNodeDto parent = getHelper().createTreeNode();
		IdmTreeNodeDto parentTwo = getHelper().createTreeNode(getHelper().createName(), parent);
		IdmTreeNodeDto position = getHelper().createTreeNode(getHelper().createName(), parentTwo);

		IdmRoleDto role = prepareRoleWithEvaluator(parentTwo); // Evaluator is on parentTwo
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(parent); // Position is sub child


		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		changePosition(identity, position);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testSubpositionMoreContract() {
		IdmTreeNodeDto parent = getHelper().createTreeNode();
		IdmTreeNodeDto parentTwo = getHelper().createTreeNode(getHelper().createName(), parent);
		IdmTreeNodeDto position = getHelper().createTreeNode(getHelper().createName(), parentTwo);

		IdmRoleDto role = prepareRoleWithEvaluator(parentTwo); // Evaluator is on parentTwo
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(parent); // Position is sub child


		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		IdmIdentityContractDto newContract = getHelper().createIdentityContact(identity, position);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
		} finally {
			getHelper().logout();
		}

		identityContractService.delete(newContract);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}
	}

	@Test(expected = ForbiddenEntityException.class)
	public void testSaveContract() {
		IdmTreeNodeDto position = getHelper().createTreeNode();
		IdmRoleDto role = prepareRoleWithEvaluator(position);
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position);

		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);

			IdmIdentityDto get = identityService.get(identity.getId(), IdmBasePermission.READ);
			get.setDescription(getHelper().createName());
			assertNotNull(get);
			get = identityService.save(get, IdmBasePermission.UPDATE);
			fail("Save identity isn't possible");
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testMoreTreeTypeStructure() {
		IdmTreeTypeDto typeOne = getHelper().createTreeType();
		IdmTreeTypeDto typeTwo = getHelper().createTreeType();
		
		IdmTreeNodeDto parent = getHelper().createTreeNode(typeOne, null);
		IdmTreeNodeDto subParent = getHelper().createTreeNode(typeOne, getHelper().createName(), parent);
		IdmTreeNodeDto position = getHelper().createTreeNode(typeOne, getHelper().createName(), subParent);

		IdmTreeNodeDto parentTwo = getHelper().createTreeNode(typeTwo, null);
		IdmTreeNodeDto subParentTwo = getHelper().createTreeNode(typeTwo, getHelper().createName(), parentTwo);
		IdmTreeNodeDto positionTwo = getHelper().createTreeNode(typeTwo, getHelper().createName(), subParentTwo);

		IdmRoleDto role = prepareRoleWithEvaluator(parent); // Evaluator is on parentTwo
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position); // Position is sub child

		// New position on another tree type
		IdmIdentityDto identityTwo = prepareIdentityOnWorkPosition(positionTwo);

		IdmIdentityDto administrator = getHelper().createIdentity();
		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			// only myself
			assertEquals(0, users.size());
			checkIdentityInList(identity, users, false);
			checkIdentityInList(identityTwo, users, false);
		} finally {
			getHelper().logout();
		}

		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);
			checkIdentityInList(identityTwo, users, false);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testCheckPermissions() {
		IdmTreeNodeDto position = getHelper().createTreeNode();
		// Only read
		IdmRoleDto role = prepareRoleWithEvaluator(position);
		IdmIdentityDto identity = prepareIdentityOnWorkPosition(position);

		IdmIdentityDto administrator = getHelper().createIdentity();
		getHelper().assignRoles(getHelper().getPrimeContract(administrator), role);

		try {
			getHelper().login(administrator);
			List<IdmIdentityDto> users = identityService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, users.size());
			checkIdentityInList(identity, users, true);

			Set<String> permissions = identityService.getPermissions(users.get(0));
			assertEquals(1, permissions.size());
			assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			getHelper().logout();
		}
	}

	/**
	 * Check if exists (or not) given identity in list
	 *
	 * @param identity
	 * @param list
	 * @param exists
	 */
	private void checkIdentityInList(IdmIdentityDto identity, List<IdmIdentityDto> list, boolean exists) {
		IdmIdentityDto founded = list.stream().filter(i -> i.getId().equals(identity.getId())).findAny().orElse(null);
		if (exists) {
			assertNotNull(founded);
		} else {
			assertNull(founded);
		}
	}

	/**
	 * Prepare role with evaluator {@link IdentityByTreeNodeEvaluator}
	 *
	 * @param position
	 * @return
	 */
	private IdmRoleDto prepareRoleWithEvaluator(IdmTreeNodeDto position) {
		IdmRoleDto role = getHelper().createRole();
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(IdentityByTreeNodeEvaluator.PARAMETER_TREE_NODE, position.getId());
		getHelper().createAuthorizationPolicy(role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdentityByTreeNodeEvaluator.class,
				properties,
				IdmBasePermission.READ);
		return role;
	}

	/**
	 * Prepare identity with given work position on main contract
	 *
	 * @param position
	 * @return
	 */
	private IdmIdentityDto prepareIdentityOnWorkPosition(IdmTreeNodeDto position) {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		assertNotNull(contract); // Something strange with test settings?

		contract.setWorkPosition(position.getId());
		contract = identityContractService.save(contract);
		return identity;
	}

	/**
	 * Change positon for main contract
	 *
	 * @param identity
	 * @param newPosition
	 */
	private void changePosition(IdmIdentityDto identity, IdmTreeNodeDto newPosition) {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		assertNotNull(contract); // Something strange with test settings?
		contract.setWorkPosition(newPosition.getId());
		contract = identityContractService.save(contract);
	}
}
