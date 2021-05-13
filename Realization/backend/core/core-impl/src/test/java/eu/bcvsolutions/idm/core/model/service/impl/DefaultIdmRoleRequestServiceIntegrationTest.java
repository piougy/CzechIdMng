package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestByIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

/**
 * Test for change permissions via Role request.
 * 
 * @author svandav
 *
 */
public class DefaultIdmRoleRequestServiceIntegrationTest extends AbstractCoreWorkflowIntegrationTest {

	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	@Qualifier(SchedulerConfiguration.TASK_EXECUTOR_NAME)
	private Executor executor;
	// FIXME: move to wf configuration / constants somewhere
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private final static String IP = "IP";

	@Before
	public void init() {
		loginAsAdmin();
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, true);
	}

	@After
	public void logout() {
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		super.logout();
	}

	@Test
	@Transactional
	public void addPermissionViaRoleRequestTest() {
		IdmIdentityDto testA = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleA = getHelper().createRole(100);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED); // can not be saved (after
														// create must be
														// CONCEPT)
		request = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());

		LocalDate validFrom = LocalDate.now().minusDays(1);
		LocalDate validTill = LocalDate.now().plusMonths(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setState(RoleRequestState.EXECUTED); // can not be saved (after
														// create must be
														// CONCEPT)
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(validTill);
		conceptA.setIdentityContract(contractA.getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		Assert.assertEquals(RoleRequestState.CONCEPT, conceptA.getState());

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(validTill, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA.getId(), identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA.getId(), identityRoles.get(0).getRole());

	}

	@Test
	@Transactional
	public void changePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentityDto testA = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleA = getHelper().createRole(100);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());
		getHelper().createIdentityRole(testA, roleA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());

		LocalDate validFrom = LocalDate.now().minusDays(1);
		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole());
		conceptA.setOperation(ConceptRoleRequestOperation.UPDATE);
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(null);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(null, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA.getId(), identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA.getId(), identityRoles.get(0).getRole());

	}

	@Test
	@Transactional
	public void removePermissionViaRoleRequestTest() {
		this.addPermissionViaRoleRequestTest();
		IdmIdentityDto testA = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleA = getHelper().createRole(100);
		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());
		getHelper().createIdentityRole(testA, roleA);

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setRole(identityRoles.get(0).getRole());
		conceptA.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptA.setIdentityContract(contractA.getId());
		conceptA.setIdentityRole(identityRoles.get(0).getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(0, identityRoles.size());

	}

	@Test(expected = RoleRequestException.class)
	@Transactional
	public void noSameApplicantExceptionTest() {
		IdmIdentityDto testA = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleA = getHelper().createRole(100);
		IdmIdentityDto testB = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contractB = identityContractService.getPrimeContract(testB.getId());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setIdentityContract(contractB.getId()); // Contract from
															// applicant B
		conceptA = conceptRoleRequestService.save(conceptA);

		// excepted ROLE_REQUEST_APPLICANTS_NOT_SAME exception
		getHelper().startRequestInternal(request, true, true);

	}

	@Test(expected = RoleRequestException.class)
	@Transactional
	public void notRightForExecuteImmediatelyExceptionTest() {
		IdmIdentityDto testA = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleA = getHelper().createRole(100);
		
		this.logout();
		// Log as user without right for immediately execute role request (without
		// approval)
		Collection<GrantedAuthority> authorities = IdmAuthorityUtils
				.toAuthorities(moduleService.getAvailablePermissions()).stream().filter(authority -> {
					return !CoreGroupPermission.ROLE_REQUEST_EXECUTE.equals(authority.getAuthority())
							&& !CoreGroupPermission.ROLE_REQUEST_ADMIN.equals(authority.getAuthority())
							&& !IdmGroupPermission.APP_ADMIN.equals(authority.getAuthority());
				}).collect(Collectors.toList());
		SecurityContextHolder.getContext().setAuthentication(
				new IdmJwtAuthentication(testA, null, authorities, "test"));

		IdmIdentityContractDto contractA = identityContractService.getPrimeContract(testA.getId());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(testA.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());

		IdmConceptRoleRequestDto conceptA = new IdmConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setIdentityContract(contractA.getId());
		conceptA = conceptRoleRequestService.save(conceptA);

		Assert.assertEquals(RoleRequestState.CONCEPT, conceptA.getState());

		// We expect exception state (we don`t have right for execute without approval)
		getHelper().startRequestInternal(request, true, true);

	}

	@Test
	@Transactional
	public void testCopyRolesByIdentity() {
		IdmIdentityDto identityDto = this.getHelper().createIdentity((GuardedString) null);

		IdmRoleDto roleOne = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		IdmIdentityContractDto primeContract = this.getHelper().getPrimeContract(identityDto);
		this.getHelper().createIdentityRole(primeContract, roleOne);
		this.getHelper().createIdentityRole(primeContract, roleTwo);

		List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identityDto.getId());
		List<UUID> identityRolesId = allByIdentity.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmIdentityDto newIdentity = this.getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto newIdentityContract = this.getHelper().getPrimeContract(newIdentity);

		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(newIdentityContract);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(newIdentityContract.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);

		assertNotNull(copyRolesByIdentity);
		assertEquals(createdRequest.getId(), copyRolesByIdentity.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(2, concepts.size());

		IdmConceptRoleRequestDto conceptOne = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleOne.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptOne);

		IdmConceptRoleRequestDto conceptTwo = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleTwo.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptTwo);
	}
	
	@Test
	public void testCopyRolesByIdentityWithoutTestTransaction() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = this.getHelper().createRole();
		for (int i = 0; i < 21; i++) {
			this.getHelper().createIdentityRole(identity, role);
		}

		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(21, assignedRoles.size());
		List<UUID> identityRolesId = assignedRoles.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmIdentityDto identityCopy = this.getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contractCopy = getHelper().getPrimeContract(identityCopy);

		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(contractCopy);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(contractCopy.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);
		Assert.assertNotNull(copyRolesByIdentity);
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(21, concepts.size());
	}

	@Test
	@Transactional
	public void testCopyRolesByIdentityWithValid() {
		LocalDate validFrom = LocalDate.now().minusDays(5);
		LocalDate validTill = LocalDate.now().plusDays(55);

		IdmIdentityDto identityDto = this.getHelper().createIdentity((GuardedString) null);

		IdmRoleDto roleOne = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		IdmIdentityContractDto primeContract = this.getHelper().getPrimeContract(identityDto);
		this.getHelper().createIdentityRole(primeContract, roleOne);
		this.getHelper().createIdentityRole(primeContract, roleTwo);

		IdmIdentityDto newIdentity = this.getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto newIdentityContract = this.getHelper().getPrimeContract(newIdentity);

		List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identityDto.getId());
		List<UUID> identityRolesId = allByIdentity.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(newIdentityContract);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(newIdentityContract.getId());
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setValidFrom(validFrom);
		requestByIdentityDto.setValidTill(validTill);
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);

		assertNotNull(copyRolesByIdentity);
		assertEquals(createdRequest.getId(), copyRolesByIdentity.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(2, concepts.size());

		IdmConceptRoleRequestDto conceptOne = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleOne.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptOne);
		assertEquals(validFrom, conceptOne.getValidFrom());
		assertEquals(validTill, conceptOne.getValidTill());

		IdmConceptRoleRequestDto conceptTwo = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleTwo.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptTwo);
		assertEquals(validFrom, conceptTwo.getValidFrom());
		assertEquals(validTill, conceptTwo.getValidTill());
	}

	@Test
	@Transactional
	public void testCopyRolesWithParameters() {
		String attributeOneCode = "attr-" + System.currentTimeMillis();
		String attributeOneDefaultValue = "test-one-" + System.currentTimeMillis();
		String attributeTwoCode = "attr-two-" + System.currentTimeMillis();
		String attributeTwoDefaultValue = "test-" + System.currentTimeMillis();
		// Prepare identity, role and parameters
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmFormAttributeDto attributeOne = new IdmFormAttributeDto(attributeOneCode);
		attributeOne.setPersistentType(PersistentType.SHORTTEXT);
		attributeOne.setRequired(true);
		attributeOne.setDefaultValue(attributeOneDefaultValue);

		IdmFormAttributeDto attributeTwo = new IdmFormAttributeDto(attributeTwoCode);
		attributeTwo.setPersistentType(PersistentType.SHORTTEXT);
		attributeTwo.setRequired(true);
		attributeTwo.setDefaultValue(attributeTwoDefaultValue);

		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class,
				ImmutableList.of(attributeOne, attributeTwo));
		roleOne.setIdentityRoleAttributeDefinition(definition.getId());
		roleOne = roleService.save(roleOne);
		IdmRoleDto roleOneFinal = roleOne;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleOneFinal, attribute);
		});

		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		this.getHelper().createIdentityRole(identityContact, roleOne);
		this.getHelper().createIdentityRole(identityContact, roleTwo);

		// Assign roles by identity
		IdmIdentityDto identityDto = this.getHelper().createIdentity((GuardedString) null);

		List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		List<UUID> identityRolesId = allByIdentity.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmIdentityContractDto identityContractDto = getHelper().createContract(identityDto);
		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(identityContractDto);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(identityContractDto.getId());
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setCopyRoleParameters(true);
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);

		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(2, concepts.size());

		UUID roleOneId = roleOne.getId();
		IdmConceptRoleRequestDto conceptOne = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleOneId);
		}).findAny().orElse(null);
		assertNotNull(conceptOne);

		IdmConceptRoleRequestDto conceptTwo = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleTwo.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptTwo);

		IdmFormInstanceDto formInstanceOne = conceptRoleRequestService.getRoleAttributeValues(conceptOne, false);
		IdmFormInstanceDto formInstanceTwo = conceptRoleRequestService.getRoleAttributeValues(conceptTwo, false);
		assertNotNull(formInstanceOne);
		assertNull(formInstanceTwo);

		assertEquals(2, formInstanceOne.getValues().size());

		attributeOne = definition.getFormAttributes().stream().filter(att -> {
			return att.getCode().equals(attributeOneCode);
		}).findFirst().orElse(null);
		attributeTwo = definition.getFormAttributes().stream().filter(att -> {
			return att.getCode().equals(attributeTwoCode);
		}).findFirst().orElse(null);
		assertNotNull(attributeOne);
		assertNotNull(attributeTwo);

		UUID attributeOneId = attributeOne.getId();
		IdmFormValueDto valueDto = formInstanceOne.getValues().stream().filter(value -> {
			return value.getFormAttribute().equals(attributeOneId);
		}).findFirst().orElse(null);
		assertNotNull(valueDto);
		assertEquals(attributeOneDefaultValue, valueDto.getValue());

		UUID attributeTwoId = attributeTwo.getId();
		valueDto = formInstanceOne.getValues().stream().filter(value -> {
			return value.getFormAttribute().equals(attributeTwoId);
		}).findFirst().orElse(null);
		assertNotNull(valueDto);
		assertEquals(attributeTwoDefaultValue, valueDto.getValue());
	}

	@Test
	@Transactional
	public void testCopyRolesWithParametersWithoutValues() {
		String attributeOneCode = "attr-" + System.currentTimeMillis();
		String attributeTwoCode = "attr-two-" + System.currentTimeMillis();
		String attributeThreeCode = "attr-three-" + System.currentTimeMillis();
		// Prepare identity, role and parameters
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmFormAttributeDto attributeOne = new IdmFormAttributeDto(attributeOneCode);
		attributeOne.setPersistentType(PersistentType.SHORTTEXT);
		attributeOne.setRequired(false);

		IdmFormAttributeDto attributeTwo = new IdmFormAttributeDto(attributeTwoCode);
		attributeTwo.setPersistentType(PersistentType.SHORTTEXT);
		attributeTwo.setRequired(false);

		IdmFormAttributeDto attributeThree = new IdmFormAttributeDto(attributeThreeCode);
		attributeThree.setPersistentType(PersistentType.SHORTTEXT);
		attributeThree.setRequired(true);

		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class,
				ImmutableList.of(attributeOne, attributeTwo, attributeThree));
		roleOne.setIdentityRoleAttributeDefinition(definition.getId());
		roleOne = roleService.save(roleOne);
		IdmRoleDto roleOneFinal = roleOne;
		definition.getFormAttributes().forEach(attribute -> {
			if (!attributeThreeCode.equals(attribute.getCode())) {
				roleFormAttributeService.addAttributeToSubdefintion(roleOneFinal, attribute);
			}
		});

		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		this.getHelper().createIdentityRole(identityContact, roleOne);
		this.getHelper().createIdentityRole(identityContact, roleTwo);

		// Assign roles by identity
		IdmIdentityDto identityDto = this.getHelper().createIdentity((GuardedString) null);

		List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		List<UUID> identityRolesId = allByIdentity.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmIdentityContractDto identityContractDto = getHelper().createContract(identityDto);
		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(identityContractDto);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(identityContractDto.getId());
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setCopyRoleParameters(true);
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);

		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(2, concepts.size());

		UUID roleOneId = roleOne.getId();
		IdmConceptRoleRequestDto conceptOne = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleOneId);
		}).findAny().orElse(null);
		assertNotNull(conceptOne);

		IdmConceptRoleRequestDto conceptTwo = concepts.stream().filter(concept -> {
			return concept.getRole().equals(roleTwo.getId());
		}).findAny().orElse(null);
		assertNotNull(conceptTwo);

		IdmFormInstanceDto formInstanceOne = conceptRoleRequestService.getRoleAttributeValues(conceptOne, false);
		IdmFormInstanceDto formInstanceTwo = conceptRoleRequestService.getRoleAttributeValues(conceptTwo, false);
		assertNotNull(formInstanceOne);
		assertTrue(formInstanceOne.getValues().isEmpty());
		assertNull(formInstanceTwo);
	}

	@Test
	@Transactional
	public void testCopyRolesWithParameterAttachment() {
		long countBefore = attachmentManager.find(null).getTotalElements();
		String attributeCode = "attr-" + System.currentTimeMillis();

		// Prepare identity, role and parameters
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmFormAttributeDto attribute = new IdmFormAttributeDto(attributeCode);
		attribute.setPersistentType(PersistentType.ATTACHMENT);

		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class,
				ImmutableList.of(attribute));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		final IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attr -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attr);
		});

		attribute = formService.getAttribute(definition, attributeCode);
		assertNotNull(attribute);

		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmIdentityRoleDto identityRoleDto = this.getHelper().createIdentityRole(identityContact, role);

		// Add attachment to identity role
		String originalContent = "test-content-" + System.currentTimeMillis();
		IdmAttachmentDto attachment = prepareAttachment(originalContent);
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment = attachmentManager.saveAttachment(null, attachment);

		List<IdmFormInstanceDto> eavs = identityRoleDto.getEavs();
		IdmFormInstanceDto formInstanceDto = eavs.get(0);
		IdmFormValueDto newValue = new IdmFormValueDto(attribute);
		newValue.setShortTextValue(attachment.getName());
		newValue.setUuidValue(attachment.getId());

		formInstanceDto.setValues(Lists.newArrayList(newValue));
		identityRoleDto.setEavs(Lists.newArrayList(formInstanceDto));

		identityRoleDto = identityRoleService.save(identityRoleDto);

		IdmFormInstanceDto identityRoleValues = identityRoleService.getRoleAttributeValues(identityRoleDto);
		identityRoleValues = identityRoleService.getRoleAttributeValues(identityRoleDto);
		List<IdmFormValueDto> values = identityRoleValues.getValues();
		assertEquals(1, values.size());
		IdmFormValueDto originalValue = values.get(0);

		// Assign roles by identity
		IdmIdentityDto identityDto = this.getHelper().createIdentity((GuardedString) null);

		List<IdmIdentityRoleDto> allByIdentity = identityRoleService.findAllByIdentity(identity.getId());
		List<UUID> identityRolesId = allByIdentity.stream().map(IdmIdentityRoleDto::getId).collect(Collectors.toList());

		IdmIdentityContractDto identityContractDto = getHelper().createContract(identityDto);
		IdmRoleRequestDto createdRequest = roleRequestService.createRequest(identityContractDto);

		IdmRoleRequestByIdentityDto requestByIdentityDto = new IdmRoleRequestByIdentityDto();
		requestByIdentityDto.setIdentityContract(identityContractDto.getId());
		requestByIdentityDto.setRoleRequest(createdRequest.getId());
		requestByIdentityDto.setIdentityRoles(identityRolesId);
		requestByIdentityDto.setCopyRoleParameters(true);
		IdmRoleRequestDto copyRolesByIdentity = roleRequestService.copyRolesByIdentity(requestByIdentityDto);

		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
				.findAllByRoleRequest(copyRolesByIdentity.getId());
		assertEquals(1, concepts.size());

		IdmConceptRoleRequestDto concept = concepts.stream().filter(cntp -> {
			return cntp.getRole().equals(roleFinal.getId());
		}).findAny().orElse(null);
		assertNotNull(concept);

		IdmFormInstanceDto formInstance = conceptRoleRequestService.getRoleAttributeValues(concept, false);
		values = formInstance.getValues();
		assertEquals(1, values.size());

		IdmFormValueDto copyValue = values.get(0);
		assertEquals(originalValue.getPersistentType(), copyValue.getPersistentType());
		assertEquals(originalValue.getFormAttribute(), copyValue.getFormAttribute());
		assertNotEquals(originalValue.getUuidValue(), copyValue.getUuidValue());

		assertEquals(countBefore + 2, attachmentManager.find(null).getTotalElements());
	}

	@Test
	@Transactional
	public void testBulkSubRolesInOneRequest() {
		int subRolesCount = 50;
		IdmRoleDto superior = getHelper().createRole();
		for (int i = 1; i <= subRolesCount; i++) {
			IdmRoleDto sub = getHelper().createRole();
			getHelper().createRoleComposition(superior, sub);
		}
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleRequestDto request = getHelper().assignRoles(getHelper().getPrimeContract(identity), false, superior);
		//
		Assert.assertEquals(subRolesCount + 1, request.getConceptRoles().size());
		Assert.assertEquals(subRolesCount + 1, identityRoleService.findAllByIdentity(identity.getId()).size());
	}
	
	@Test
	public void testExecuteConcurentRoleRequests() {
		// prepare two requests with assigned roles
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmRoleDto role = getHelper().createRole();
		//
		for (int i = 0; i < 10; i++) {
			getHelper().createIdentityRole(contract, role);
		}
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findValidRoles(identity.getId(), null).getContent();
		Assert.assertEquals(10, assignedRoles.size());
		//
		IdmRoleRequestDto requestOne = createDeleteRoleRequest(contract, assignedRoles);
		IdmRoleRequestDto requestTwo = createDeleteRoleRequest(contract, assignedRoles);
		//
		// execute request in two threads
		FutureTask<?> taskOne = new FutureTask<Boolean>(() -> { 
			roleRequestService.startRequest(requestOne.getId(), false);
			return true; 
		});
		FutureTask<?> taskTwo = new FutureTask<Boolean>(() -> { 
			roleRequestService.startRequest(requestTwo.getId(), false);
			return true; 
		});
		executor.execute(taskOne);
		executor.execute(taskTwo);
		//
		while (true) {
			if (taskOne.isDone() && taskTwo.isDone()){
				break;
			}
		}
		//
		IdmRoleRequestDto executedRequestOne = roleRequestService.get(requestOne, new IdmRoleRequestFilter(true));
		IdmRoleRequestDto executedRequestTwo = roleRequestService.get(requestTwo, new IdmRoleRequestFilter(true));
		// One of requests ends with exception, but can be read => referential integrity is ok
		Assert.assertTrue(executedRequestOne.getState().isTerminatedState());
		Assert.assertTrue(executedRequestTwo.getState().isTerminatedState());
	}
	
	@Test
	@Transactional
	public void testUpdateSubRoleByRequest() {
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto sub = getHelper().createRole();
		getHelper().createRoleComposition(superior, sub);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmRoleRequestDto request = getHelper().assignRoles(getHelper().getPrimeContract(identity), false, superior);
		//
		Assert.assertEquals(2, request.getConceptRoles().size());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidTill() == null));
		IdmIdentityRoleDto directlyAssignedRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(superior.getId(), directlyAssignedRole.getRole());
		//
		// change superior role validity by concept
		request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED);
		request = roleRequestService.save(request);
		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());
		LocalDate validTill = LocalDate.now().plusMonths(1);
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setState(RoleRequestState.EXECUTED);
		concept.setOperation(ConceptRoleRequestOperation.UPDATE);
		concept.setRole(superior.getId());
		concept.setIdentityRole(directlyAssignedRole.getId());
		concept.setValidTill(validTill);
		concept.setIdentityContract(directlyAssignedRole.getIdentityContract());
		concept = conceptRoleRequestService.save(concept);

		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidTill().equals(validTill)));
		//
		// and remove
		request = new IdmRoleRequestDto();
		request.setApplicant(identity.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED);
		request = roleRequestService.save(request);
		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());
		concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setState(RoleRequestState.EXECUTED);
		concept.setOperation(ConceptRoleRequestOperation.REMOVE);
		concept.setRole(superior.getId());
		concept.setIdentityRole(directlyAssignedRole.getId());
		concept.setIdentityContract(directlyAssignedRole.getIdentityContract());
		concept = conceptRoleRequestService.save(concept);

		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testExecuteRequestForSameAutomaticRole() {
		IdmRoleDto role = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		//
		// create two requests
		IdmRoleRequestDto requestOne = new IdmRoleRequestDto();
		requestOne.setApplicant(identity.getId());
		requestOne.setExecuteImmediately(true);
		requestOne.setRequestedByType(RoleRequestedByType.MANUALLY);
		requestOne.setState(RoleRequestState.EXECUTED);
		requestOne = roleRequestService.save(requestOne);
		Assert.assertEquals(RoleRequestState.CONCEPT, requestOne.getState());
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(requestOne.getId());
		concept.setState(RoleRequestState.EXECUTED);
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(role.getId());
		concept.setAutomaticRole(automaticRole.getId());
		concept.setIdentityContract(primeContract.getId());
		concept = conceptRoleRequestService.save(concept);
		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());
		//
		// create two requests
		IdmRoleRequestDto requestTwo = new IdmRoleRequestDto();
		requestTwo.setApplicant(identity.getId());
		requestTwo.setExecuteImmediately(true);
		requestTwo.setRequestedByType(RoleRequestedByType.MANUALLY);
		requestTwo.setState(RoleRequestState.EXECUTED);
		requestTwo = roleRequestService.save(requestTwo);
		Assert.assertEquals(RoleRequestState.CONCEPT, requestTwo.getState());
		concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(requestTwo.getId());
		concept.setState(RoleRequestState.EXECUTED);
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(role.getId());
		concept.setAutomaticRole(automaticRole.getId());
		concept.setIdentityContract(primeContract.getId());
		concept = conceptRoleRequestService.save(concept);
		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());
		
		getHelper().startRequestInternal(requestOne, true, true);
		requestOne = roleRequestService.get(requestOne.getId());
		Assert.assertEquals(RoleRequestState.EXECUTED, requestOne.getState());
		getHelper().startRequestInternal(requestTwo, true, true);
		requestTwo = roleRequestService.get(requestTwo.getId());
		Assert.assertEquals(RoleRequestState.EXECUTED, requestTwo.getState());
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getAutomaticRole().equals(automaticRole.getId())));
	}
	
	@Test
	public void testExecuteRoleRequestValueAsync() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto identityContact = getHelper().createContract(identity);
		IdmRoleDto role = createRoleWithAttributes(true);
		IdmFormDefinitionDto definition = formService.getDefinition(role.getIdentityRoleAttributeDefinition());
		IdmFormAttributeDto ipAttributeDto = definition.getFormAttributes().stream() //
				.filter(attribute -> IP.equals(attribute.getCode())) //
				.findFirst() //
				.get(); //
		//
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			// Add value
			IdmFormValueDto formValue = new IdmFormValueDto(ipAttributeDto);
			formValue.setStringValue(getHelper().createName());
			formValue.setPersistentType(PersistentType.TEXT);
			formValue.setFormAttribute(ipAttributeDto.getId());
	
			IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
			formInstance.setFormDefinition(definition);
			formInstance.getValues().add(formValue);
			// Create request
			IdmRoleRequestDto request = new IdmRoleRequestDto();
			request.setApplicant(identity.getId());
			request.setRequestedByType(RoleRequestedByType.MANUALLY);
			request.setExecuteImmediately(true);
			request = roleRequestService.save(request);
			// Create concept
			IdmConceptRoleRequestDto conceptRole = new IdmConceptRoleRequestDto();
			conceptRole.setIdentityContract(identityContact.getId());
			conceptRole.setRole(role.getId());
			conceptRole.setOperation(ConceptRoleRequestOperation.ADD);
			conceptRole.setRoleRequest(request.getId());
			conceptRole.getEavs().add(formInstance);
			conceptRole = conceptRoleRequestService.save(conceptRole);
	
			// Start request
			Map<String, Serializable> variables = new HashMap<>();
			variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, Boolean.FALSE);
			RoleRequestEvent event = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, request, variables);
			event.setPriority(PriorityType.HIGH);
			//
			request = roleRequestService.startRequest(event);
			UUID requestId = request.getId();
			
			getHelper().waitForResult(res -> {
				return roleRequestService.get(requestId).getState() != RoleRequestState.EXECUTED;
			}, 500, 50);
			
			IdmRoleRequestDto roleRequestDto = roleRequestService.get(request);
			assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
			
			conceptRole = conceptRoleRequestService.get(conceptRole.getId());
			assertEquals(RoleRequestState.EXECUTED, conceptRole.getState());
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setIdentityContractId(identityContact.getId());
			
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
			assertEquals(1, identityRoles.size());
			
			IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);
			IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRoleDto);
			assertNotNull(formInstanceDto);
			List<IdmFormValueDto> values = formInstanceDto.getValues();
			
			assertEquals(1, values.size());
			assertEquals(formValue.getValue(), values.get(0).getValue());
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			// cleanup form definition
			getHelper().deleteIdentity(identity.getId());
			getHelper().deleteRole(role.getId());
			formService.deleteDefinition(definition);
		}
	}

	private IdmAttachmentDto prepareAttachment(String content) {
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("test.txt");
		attachment.setMimetype("text/plain");
		attachment.setInputData(IOUtils.toInputStream(content));
		//
		return attachment;
	}
	
	public IdmRoleRequestDto createDeleteRoleRequest(IdmIdentityContractDto contract, List<IdmIdentityRoleDto> assignedRoles) {
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(true);
		roleRequest = roleRequestService.save(roleRequest);
		//
		for (IdmIdentityRoleDto assignedRole : assignedRoles) {
			
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(assignedRole.getRole());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequest.setIdentityRole(assignedRole.getId());
			//
			conceptRoleRequestService.save(conceptRoleRequest);
		}
		return roleRequest;
	}
	
	private IdmRoleDto createRoleWithAttributes(boolean ipRequired ) {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());
		
		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(ipRequired);
		
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, ImmutableList.of(ipAttribute));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		
		return role;
	}
}
