package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleRequestControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleRequestDto> {

	@Autowired private IdmRoleRequestController controller;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleRequestDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected IdmRoleRequestDto prepareDto() {
		IdmIdentityDto applicant = getHelper().createIdentity((GuardedString) null);
		IdmRoleRequestDto dto = new IdmRoleRequestDto();
		dto.setApplicant(applicant.getId());
		dto.setState(RoleRequestState.CONCEPT);
		dto.setRequestedByType(RoleRequestedByType.MANUALLY);
		//
		return dto;
	}
	
	@Test
	public void testGetIncompatibleRoles() throws Exception {
		IdmRoleRequestDto roleRequest = createDto();
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		IdmRoleDto roleFour = getHelper().createRole();
		IdmRoleDto roleFive = getHelper().createRole();
		IdmRoleDto roleSix = getHelper().createRole();
		// assign roles
		IdmIdentityDto applicant = identityService.get(roleRequest.getApplicant());
		getHelper().createIdentityRole(applicant, roleOne);
		getHelper().createIdentityRole(applicant, roleTwo);
		getHelper().createIdentityRole(applicant, roleThree);
		// create incompatible roles definition
		getHelper().createIncompatibleRole(roleOne, roleTwo);
		getHelper().createIncompatibleRole(roleThree, roleFour);
		getHelper().createIncompatibleRole(roleOne, roleSix);
		//
		// create concepts
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(roleRequest.getId());
		concept.setIdentityContract(getHelper().getPrimeContract(applicant).getId());
		concept.setRole(roleFour.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequestService.save(concept);
		concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(roleRequest.getId());
		concept.setIdentityContract(getHelper().getPrimeContract(applicant).getId());
		concept.setRole(roleFive.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequestService.save(concept);
		//
		String response = getMockMvc().perform(get(String.format("%s/incompatible-roles", getDetailUrl(roleRequest.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = toDtos(response, ResolvedIncompatibleRoleDto.class)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(2, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleOne.getId()) && ir.getSub().equals(roleTwo.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleThree.getId()) && ir.getSub().equals(roleFour.getId());
				}));
	}
	
	@Test
	public void testGetIncompatibleRolesWithoutRemovedInConcept() throws Exception {
		IdmRoleRequestDto roleRequest = createDto();
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		IdmRoleDto roleFour = getHelper().createRole();
		IdmRoleDto roleFive = getHelper().createRole();
		IdmRoleDto roleSix = getHelper().createRole();
		// assign roles
		IdmIdentityDto applicant = identityService.get(roleRequest.getApplicant());
		getHelper().createIdentityRole(applicant, roleOne);
		getHelper().createIdentityRole(applicant, roleTwo);
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(applicant, roleThree);
		getHelper().createIdentityRole(applicant, roleFour);
		// create incompatible roles definition
		getHelper().createIncompatibleRole(roleOne, roleTwo);
		getHelper().createIncompatibleRole(roleThree, roleFour);
		getHelper().createIncompatibleRole(roleOne, roleSix);
		//
		// create concepts
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(roleRequest.getId());
		concept.setIdentityContract(getHelper().getPrimeContract(applicant).getId());
		concept.setRole(roleFour.getId());
		concept.setIdentityRole(identityRole.getId());
		concept.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptRoleRequestService.save(concept);
		concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(roleRequest.getId());
		concept.setIdentityContract(getHelper().getPrimeContract(applicant).getId());
		concept.setRole(roleFive.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequestService.save(concept);
		//
		String response = getMockMvc().perform(get(String.format("%s/incompatible-roles", getDetailUrl(roleRequest.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = toDtos(response, ResolvedIncompatibleRoleDto.class)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(1, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleOne.getId()) && ir.getSub().equals(roleTwo.getId());
				}));
	}
}
