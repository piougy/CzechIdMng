package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByRoleEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCanBeRequestedEvaluator;

/**
 * Controller tests
 * - CRUD
 * - filter tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityRoleDto> {

	@Autowired private IdmIdentityRoleController controller;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityRoleDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}

	@Override
	protected IdmIdentityRoleDto prepareDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		dto.setIdentityContractDto(getHelper().getPrimeContract(getHelper().createIdentity().getId()));
		dto.setRole(getHelper().createRole().getId());
		dto.setValidFrom(LocalDate.now());
		dto.setValidTill(LocalDate.now().plusDays(1));
		return dto;
	}
	
	@Test
	public void testFindByText() {
		// username
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, getHelper().createRole());
		IdmIdentityDto other = getHelper().createIdentity();
		getHelper().createIdentityRole(other, getHelper().createRole());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setText(identity.getUsername());
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(createIdentityRole.getId())));
	}
	
	@Test
	public void testFindInvalidRoles() {
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, getHelper().createRole()); // valid
		IdmIdentityRoleDto inValidByDate = getHelper().createIdentityRole(identity, getHelper().createRole(), null, LocalDate.now().minusDays(2));
		IdmIdentityContractDto invalidContract = getHelper().createIdentityContact(identity, null, null, LocalDate.now().minusDays(2));
		IdmIdentityRoleDto inValidByContract = getHelper().createIdentityRole(invalidContract, getHelper().createRole());
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setValid(Boolean.FALSE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(inValidByDate.getId())));
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(inValidByContract.getId())));
	}
	
	@Test
	public void testFindAutomaticRoles() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		IdmIdentityRoleDto normal = getHelper().createIdentityRole(contract, getHelper().createRole()); // normal
		// automatic
		IdmIdentityRoleDto automaticIdentityRole = new IdmIdentityRoleDto();
		automaticIdentityRole.setIdentityContract(contract.getId());
		automaticIdentityRole.setRole(getHelper().createRole().getId());
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(getHelper().createRole().getId());
		automaticIdentityRole.setAutomaticRole(automaticRole.getId());
		IdmIdentityRoleDto automatic = createDto(automaticIdentityRole);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		filter.setAutomaticRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(automatic.getId())));
		//
		filter.setAutomaticRole(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
		//
		// find by automatic role
		filter.setAutomaticRole(null);
		filter.setAutomaticRoleId(automaticRole.getId());
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(automatic.getId())));
	}
	
	@Test
	public void findDirectRoles() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		IdmIdentityRoleDto normal = getHelper().createIdentityRole(contract, getHelper().createRole()); // normal
		// not direct
		IdmIdentityRoleDto notDirectIdentityRole = new IdmIdentityRoleDto();
		notDirectIdentityRole.setIdentityContract(contract.getId());
		notDirectIdentityRole.setRole(getHelper().createRole().getId());
		notDirectIdentityRole.setDirectRole(normal.getId());
		IdmIdentityRoleDto notDirect = createDto(notDirectIdentityRole);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		filter.setDirectRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
		//
		filter.setDirectRole(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(notDirect.getId())));
		//
		// find by direct role
		filter.setDirectRole(null);
		filter.setDirectRoleId(normal.getId());
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(notDirect.getId())));
	}
	
	@Test
	public void findByContractPosition() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmContractPositionDto contractPositionOne = getHelper().createContractPosition(contract);
		IdmContractPositionDto contractPositionOther = getHelper().createContractPosition(contract);
		IdmIdentityRoleDto one = getHelper().createIdentityRole(contractPositionOne, getHelper().createRole());
		getHelper().createIdentityRole(contractPositionOther, getHelper().createRole()); // other
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setContractPositionId(contractPositionOne.getId());
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(one.getId())));
	}
	
	@Test
	public void testFindByRoleEnvironment() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto roleOne = getHelper().createRole(null, getHelper().createName(), getHelper().createName());
		IdmRoleDto roleTwo = getHelper().createRole(null, getHelper().createName(), getHelper().createName());
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, roleOne);
		getHelper().createIdentityRole(identity, roleTwo);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleEnvironment(roleOne.getEnvironment());
		List<IdmIdentityRoleDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
	}
	
	@Test
	public void testFindByRoleId() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, roleOne);
		getHelper().createIdentityRole(identity, roleTwo);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleId(roleOne.getId());
		List<IdmIdentityRoleDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
	}
	
	@Test
	public void testFindByRoleCatalogueId() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleCatalogueDto roleCatalogueOne = getHelper().createRoleCatalogue();
		getHelper().createRoleCatalogueRole(roleOne, roleCatalogueOne);
		IdmRoleCatalogueDto roleCatalogueTwo = getHelper().createRoleCatalogue();
		getHelper().createRoleCatalogueRole(roleTwo, roleCatalogueTwo);
		IdmIdentityRoleDto createIdentityRole = getHelper().createIdentityRole(identity, roleOne);
		getHelper().createIdentityRole(identity, roleTwo);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleCatalogueId(roleCatalogueOne.getId());
		List<IdmIdentityRoleDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(createIdentityRole.getId())));
	}
	
	@Test
	public void testFindValidRoles() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityRoleDto validRole = getHelper().createIdentityRole(identity, getHelper().createRole()); // valid
		getHelper().createIdentityRole(identity, getHelper().createRole(), null, LocalDate.now().minusDays(2)); // inValidByDate
		IdmIdentityContractDto invalidContract = getHelper().createIdentityContact(identity, null, null, LocalDate.now().minusDays(2));
		getHelper().createIdentityRole(invalidContract, getHelper().createRole()); // inValidByContract
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setValid(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(validRole.getId())));
	}
	
	@Test
	public void testFindByRoleComposition() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		//
		IdmRoleCompositionDto roleCompositionOne = getHelper().createRoleComposition(roleOne, roleTwo);
		getHelper().createRoleComposition(roleTwo, roleThree);
		//
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityRoleDto directRole = getHelper().createIdentityRole(identity, roleOne);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setRoleCompositionId(roleCompositionOne.getId());
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getDirectRole().equals(directRole.getId())
				&& ir.getRole().equals(roleTwo.getId())));
	}
	
	@Test
	public void testFindCanBeRequestedRoles() throws Exception {
		IdmRoleDto roleOne = createRole(true);
		IdmRoleDto roleTwo = createRole(false); // other
		//
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto assignedRole = getHelper().createRole();
		//
		getHelper().createIdentityRole(identity, assignedRole);
		// 
		// other identity - their identity roles we will read
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		getHelper().createIdentityRole(identityTwo, roleOne);
		getHelper().createIdentityRole(identityTwo, roleTwo);
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				assignedRole.getId(),
				CoreGroupPermission.ROLE,
				IdmRole.class,
				RoleCanBeRequestedEvaluator.class,
				RoleBasePermission.CANBEREQUESTED, IdmBasePermission.UPDATE, IdmBasePermission.READ);
		// with update transitively
		ConfigurationMap evaluatorProperties = new ConfigurationMap();
		evaluatorProperties.put(IdentityRoleByRoleEvaluator.PARAMETER_CAN_BE_REQUESTED_ONLY, false);
		IdmAuthorizationPolicyDto transientIdentityRolePolicy = getHelper().createAuthorizationPolicy(
				assignedRole.getId(),
				CoreGroupPermission.IDENTITYROLE,
				IdmIdentityRole.class,
				IdentityRoleByRoleEvaluator.class,
				evaluatorProperties);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identityTwo.getId());
		List<IdmIdentityRoleDto> identityRoles = find("can-be-requested", filter, getAuthentication(identity.getUsername()));
		//
		Assert.assertFalse(identityRoles.isEmpty());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().anyMatch(r -> r.getRole().equals(roleOne.getId())));
		//
		List<String> permissions = getPermissions(identityRoles.get(0), getAuthentication(identity.getUsername()));
		//
		Assert.assertEquals(3, permissions.size());
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(RoleBasePermission.CANBEREQUESTED.name())));
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
		Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		//
		// can be requested only
		evaluatorProperties = new ConfigurationMap();
		evaluatorProperties.put(IdentityRoleByRoleEvaluator.PARAMETER_CAN_BE_REQUESTED_ONLY, true);
		transientIdentityRolePolicy.setEvaluatorProperties(evaluatorProperties);
		authorizationPolicyService.save(transientIdentityRolePolicy);
		//
		identityRoles = find("can-be-requested", filter, getAuthentication(identity.getUsername()));
		//
		Assert.assertFalse(identityRoles.isEmpty());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().anyMatch(r -> r.getRole().equals(roleOne.getId())));
		//
		// read authority is not available now
		try {
			getHelper().login(identity);
			//
			Set<String> canBeRequestedPermissions = identityRoleService.getPermissions(identityRoles.get(0).getId());
			//		
			Assert.assertEquals(1, canBeRequestedPermissions.size());
			Assert.assertTrue(canBeRequestedPermissions.stream().anyMatch(p -> p.equals(RoleBasePermission.CANBEREQUESTED.name())));
		} finally {
			logout();
		}
	}
	
	private IdmRoleDto createRole( boolean canBeRequested) {
		IdmRoleDto role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setName(role.getCode());
		role.setCanBeRequested(canBeRequested);
		//
		return roleService.save(role);
	}
	
	@Test
	@Ignore
	@Override
	public void testSaveFormDefinition() throws Exception {
		// We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
	}
	
	@Test
	@Ignore
	@Override
	public void testSaveFormValue() throws Exception {
		// We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
	}
	
	@Test
	@Ignore
	@Override
	public void testDownloadFormValue() throws Exception {
		// We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
	}
	
	@Test
	@Ignore
	@Override
	public void testPreviewFormValue() throws Exception {
		// We don't want testing form definition - IdentityRole has extra behavior (role attributes) for Form (definition is changing by role)
	}
}
