package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - crud
 * - filters
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleRequestControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleRequestDto> {

	@Autowired private IdmRoleRequestController controller;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleRequestService roleRequestService;
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
		Assert.assertEquals(1, incompatibleRoles.size());
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
		Assert.assertEquals(0, incompatibleRoles.size());
	}
	
	@Test
	public void testFilterByCreatorId() {
		IdmIdentityDto creatorOne = getHelper().createIdentity();
		IdmIdentityDto creatorTwo = getHelper().createIdentity();
		
		IdmRoleRequestDto roleRequestOne = null;
		IdmRoleRequestDto roleRequesttwo = null;
		try {
			getHelper().login(creatorOne);
			roleRequestOne = roleRequestService.save(prepareDto());
		} finally {
			getHelper().logout();
		}
		try {
			getHelper().login(creatorTwo);
			roleRequesttwo = roleRequestService.save(prepareDto());
		} finally {
			getHelper().logout();
		}
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("creator", creatorOne.getId().toString());
		List<IdmRoleRequestDto> requests = find(filter);
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(roleRequestOne.getId(), requests.get(0).getId());
		//
		filter.set("creator", creatorTwo.getId().toString());
		requests = find(filter);
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(roleRequesttwo.getId(), requests.get(0).getId());
	}
	
	@Test
	public void testFilterByApplicantId() {
		IdmRoleRequestDto roleRequestOne = createDto();
		IdmRoleRequestDto roleRequesttwo = createDto();
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("applicantId", roleRequestOne.getApplicant().toString());
		List<IdmRoleRequestDto> requests = find(filter);
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(roleRequestOne.getId(), requests.get(0).getId());
		//
		filter.set("applicantId", roleRequesttwo.getApplicant().toString());
		requests = find(filter);
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(roleRequesttwo.getId(), requests.get(0).getId());
	}
	
	@Test
	public void testFindByWrongApplicant() throws Exception {
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("applicant", "not-exist-name");
		List<IdmRoleRequestDto> results = find(filter);
		//
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testFindByState() throws Exception {
		IdmRoleRequestDto requestOne = this.createDto();
		requestOne.setState(RoleRequestState.EXECUTED);
		requestOne = createDto(requestOne);
		IdmRoleRequestDto requestTwo = this.createDto();
		requestTwo.setState(RoleRequestState.CANCELED);
		requestTwo.setApplicant(requestOne.getApplicant());
		requestTwo = createDto(requestTwo);
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("state", RoleRequestState.EXECUTED.name());
		filter.set("applicant", requestOne.getApplicant().toString());
		//
		List<IdmRoleRequestDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(requestOne.getId(), results.get(0).getId());
	}
	
	@Test
	public void testFilterByCreated() throws Exception {
		IdmRoleRequestDto requestOne = this.createDto();
		getHelper().waitForResult(null, 1, 1);
		IdmRoleRequestDto requestTwo = this.createDto();

		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("createdFrom", requestTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).toString());
		filter.set("createdTill", ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS).toString());
		filter.put("applicants", Lists.newArrayList(requestOne.getApplicant().toString(), requestTwo.getApplicant().toString()));

		List<IdmRoleRequestDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(requestTwo.getId(), results.get(0).getId());
	}
	
	@Test
	public void testFilterByApplicant() throws Exception {
		IdmIdentityDto applicantOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto applicantTwo = getHelper().createIdentity((GuardedString) null);

		IdmRoleRequestDto request = this.createDto();
		request.setApplicant(applicantOne.getId());
		IdmRoleRequestDto requestOne = createDto(request);
		request = this.createDto();
		request.setApplicant(applicantTwo.getId());
		IdmRoleRequestDto requestTwo = createDto(request);

		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.put("applicants", Lists.newArrayList(requestOne.getApplicant().toString(), requestTwo.getApplicant().toString()));

		List<IdmRoleRequestDto> results = find(filter);
		
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestTwo.getId())));

		filter.put("applicants", Lists.newArrayList(requestOne.getApplicant().toString()));
		results = find(filter);
		Assert.assertEquals(requestOne.getId(), results.get(0).getId());
	}
	
	@Test
	public void testFilterByStates() throws Exception {
		IdmRoleRequestDto request = this.createDto();
		request.setState(RoleRequestState.EXECUTED);
		IdmRoleRequestDto requestOne = createDto(request);
		request = this.createDto();
		request.setState(RoleRequestState.CANCELED);
		request.setApplicant(requestOne.getApplicant());
		IdmRoleRequestDto requestTwo = createDto(request);
		request = this.createDto();
		request.setState(RoleRequestState.APPROVED);
		request.setApplicant(requestOne.getApplicant());
		createDto(request); // other state
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.put("states", Lists.newArrayList(RoleRequestState.EXECUTED.name(), RoleRequestState.CANCELED.name()));
		filter.set("applicant", requestOne.getApplicant().toString());
		//
		List<IdmRoleRequestDto> results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestTwo.getId())));
	}
	
	@Test
	public void testFilterBySystemStates() throws Exception {
		IdmRoleRequestDto request = this.createDto();
		request.setSystemState(new OperationResultDto(OperationState.RUNNING));
		IdmRoleRequestDto requestOne = createDto(request);
		request = this.createDto();
		request.setSystemState(new OperationResultDto(OperationState.CANCELED));
		request.setApplicant(requestOne.getApplicant());
		IdmRoleRequestDto requestTwo = createDto(request);
		request = this.createDto();
		request.setSystemState(new OperationResultDto(OperationState.EXCEPTION));
		request.setApplicant(requestOne.getApplicant());
		createDto(request); // other state
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.put("systemStates", Lists.newArrayList(OperationState.RUNNING.name(), OperationState.CANCELED.name()));
		filter.set("applicant", requestOne.getApplicant().toString());
		//
		List<IdmRoleRequestDto> results = find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(requestTwo.getId())));
	}
}
