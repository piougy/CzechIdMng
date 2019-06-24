package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with request-identity-role service
 * 
 * @author Vít Švanda
 *
 */
public class IdmRequestIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRequestIdentityRoleService requestIdentityRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void testAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		
		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));
		
		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());
		
		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());
		
		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		
		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(role.getId(), identityRoles.get(0).getRole());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}
	
	@Test
	@Transactional
	public void testAssignRoles() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		IdmRoleDto roleThree = this.getHelper().createRole();
		Set<UUID> roles = Sets.newSet(role.getId(), roleTwo.getId(), roleThree.getId());
		
		// Create request for new identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setRole(role.getId());
		dto.setRoles(Sets.newSet(roleTwo.getId(), roleThree.getId()));
		dto.setValidFrom(LocalDate.now().minusDays(1));
		dto.setValidTill(LocalDate.now().plusDays(10));
		
		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());
		
		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest());
		Assert.assertNotNull(request);
		// Concepts are empty, because the request does not return them be default
		Assert.assertEquals(0, request.getConceptRoles().size());
		request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(3, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());
		
		request.getConceptRoles().forEach(concept -> {
			Assert.assertEquals(contract.getId(), concept.getIdentityContract());
			Assert.assertTrue(roles.contains(concept.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		});
		
		this.getHelper().executeRequest(request, false, true);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(3, identityRoles.size());
		identityRoles.forEach(identityRole -> {
			Assert.assertTrue(roles.contains(identityRole.getRole()));
			Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRole.getValidFrom());
			Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRole.getValidTill());
		});
	}
	
	@Test
	@Transactional
	public void testAssignRemove() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role);
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		
		// Create request for remove identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setIdentityRole(identityRoles.get(0).getId());
		dto.setId(dto.getIdentityRole());
		
		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.deleteRequestIdentityRole(dto);
		
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());
		
		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());
		
		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.REMOVE, concept.getOperation());
		
		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(0, identityRoles.size());
	}
	
	
	@Test
	@Transactional
	public void testUpdateAssignRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmRoleDto role = this.getHelper().createRole();
		this.getHelper().createIdentityRole(contract, role, LocalDate.now().minusDays(1), null);
		
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		
		// Create request for updated identity-role
		IdmRequestIdentityRoleDto dto = new IdmRequestIdentityRoleDto();
		dto.setIdentityContract(contract.getId());
		dto.setIdentityRole(identityRoles.get(0).getId());
		dto.setId(dto.getIdentityRole());
		dto.setValidFrom(LocalDate.now().minusDays(10));
		dto.setValidTill(LocalDate.now().plusDays(10));
		
		IdmRequestIdentityRoleDto createdRequestIdentityRole = requestIdentityRoleService.save(dto);
		
		Assert.assertNotNull(createdRequestIdentityRole);
		// Request must been created
		Assert.assertNotNull(createdRequestIdentityRole.getRoleRequest());
		Assert.assertEquals(role.getId(), createdRequestIdentityRole.getRole());
		Assert.assertEquals(contract.getId(), createdRequestIdentityRole.getIdentityContract());
		
		IdmRoleRequestDto request = roleRequestService.get(createdRequestIdentityRole.getRoleRequest(), new IdmRoleRequestFilter(true));
		Assert.assertNotNull(request);
		// Concepts are not empty now
		Assert.assertEquals(1, request.getConceptRoles().size());
		// Applicant must be ours identity
		Assert.assertEquals(contract.getIdentity(), request.getApplicant());
		
		IdmConceptRoleRequestDto concept = request.getConceptRoles().get(0);
		Assert.assertEquals(contract.getId(), concept.getIdentityContract());
		Assert.assertEquals(role.getId(), concept.getRole());
		Assert.assertEquals(ConceptRoleRequestOperation.UPDATE, concept.getOperation());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), concept.getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), concept.getValidTill());
		
		this.getHelper().executeRequest(request, false, true);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(createdRequestIdentityRole.getValidFrom(), identityRoles.get(0).getValidFrom());
		Assert.assertEquals(createdRequestIdentityRole.getValidTill(), identityRoles.get(0).getValidTill());
	}
}
