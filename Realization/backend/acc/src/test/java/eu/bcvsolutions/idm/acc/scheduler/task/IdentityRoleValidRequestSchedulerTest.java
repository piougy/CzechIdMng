package eu.bcvsolutions.idm.acc.scheduler.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.repository.SysSystemMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.IdentityRoleValidRequestTaskExecutor;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * 
 * @author Ondřej Kopr
 */
public class IdentityRoleValidRequestSchedulerTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleRepository roleRepository;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService idmIdentityRoleSerivce;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemMappingService mappingService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private SysSystemAttributeMappingService attributeMappingService;
	@Autowired private SysRoleSystemService sysRoleSystemService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private IdmIdentityRoleRepository identityRoleRepository;
	@Autowired private IdmLongRunningTaskService longRunningTaskService;
	@Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	@Autowired private SysSystemMappingRepository systemMappingRepository;
	//
	// local variables
	private SysSystem system = null;
	private SysSystemMappingDto systemMapping = null;
	private int MAX_CREATE = 10;
	
	@Before
	public void loginAndInit() {
		loginAsAdmin("admin");
		createAndSaveSystemWithMapping();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void createValidRole() {
		IdmIdentityDto identity = createAndSaveIdentity();
		IdmRoleDto role = createAndSaveRole();
		createAndSaveRoleSystem(role, system);
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContractDto identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate validFrom = new LocalDate();
		// set minus days
		validFrom = validFrom.minusDays(5);
		// provisioning is not executed
		createAndSaveIdentityRole(identityContract, role, null, validFrom);
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentity = identityAccountService.find(filter, null).getContent().get(0);
		// it must exists
		assertNotNull(accountIdentity);
	}
	
	@Test
	public void createNonValidRole() {
		IdmIdentityDto identity = createAndSaveIdentity();
		IdmRoleDto role = createAndSaveRole();
		createAndSaveRoleSystem(role, system);
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContractDto identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate validFrom = new LocalDate();
		// set plus days
		validFrom = validFrom.plusDays(5);
		// provisioning is not executed
		createAndSaveIdentityRole(identityContract, role, null, validFrom);
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> list = identityAccountService.find(filter, null).getContent();
		// it must not exists
		assertEquals(true, list.isEmpty());
	}
	
	@Test
	public void createNonValidRoleAndValid() throws InterruptedException, ExecutionException {
		IdmIdentityDto identity = createAndSaveIdentity();
		IdmRoleDto role = createAndSaveRole();
		createAndSaveRoleSystem(role, system);
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContractDto identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate validFrom = new LocalDate();
		// set plus days
		validFrom = validFrom.plusDays(5);
		// provisioning is not executed, role isn't valid from now
		createAndSaveIdentityRole(identityContract, role, null, validFrom);
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> list = identityAccountService.find(filter, null).getContent();
		// it must not exists
		assertEquals(true, list.isEmpty());
		//
		IdentityRoleValidRequestTaskExecutor taskExecutor1 = new IdentityRoleValidRequestTaskExecutor();
		
		LongRunningFutureTask<Boolean> futureTask1 = longRunningTaskManager.execute(taskExecutor1);
		assertEquals(true, futureTask1.getFutureTask().get());
		
		IdmLongRunningTaskDto longRunningTask1 = longRunningTaskService.get(taskExecutor1.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask1.getResult().getState());
		
		list = identityAccountService.find(filter, null).getContent();
		// still empty, role isn't valid
		assertEquals(true, list.isEmpty());
		
		List<IdmIdentityRole> roles = identityRoleRepository.findAllByIdentityContract_Identity_Id(identity.getId(), null);
		assertEquals(1, roles.size());
		IdmIdentityRole identityRole = roles.get(0);
		
		validFrom = new LocalDate();
		validFrom = validFrom.minusDays(5);
		identityRole.setValidFrom(validFrom);
		identityRoleRepository.save(identityRole);
		
		// execute again
		IdentityRoleValidRequestTaskExecutor taskExecutor2 = new IdentityRoleValidRequestTaskExecutor();

		LongRunningFutureTask<Boolean> futureTask2 = longRunningTaskManager.execute(taskExecutor2);
		
		assertEquals(true, futureTask2.getFutureTask().get());
		
		IdmLongRunningTaskDto longRunningTask2 = longRunningTaskService.get(taskExecutor2.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask2.getResult().getState());
		
		list = identityAccountService.find(filter, null).getContent();
		assertEquals(false, list.isEmpty());
		assertEquals(1, list.size());
		// newly created accounts
		assertNotNull(list.get(0));
	}
	
	@Test
	public void createLotsOfValidRequests() throws InterruptedException, ExecutionException{
		IdmRoleDto role = createAndSaveRole();
		createAndSaveRoleSystem(role, system);
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		
		LocalDate validFrom = new LocalDate();
		// set plus days
		validFrom = validFrom.plusDays(5);
		
		// clear request, if any
		List<IdmIdentityRoleValidRequestDto> list = identityRoleValidRequestService.findAllValid();
		for (IdmIdentityRoleValidRequestDto request : list) {
			identityRoleValidRequestService.delete(request);
		}
		
		List<IdmIdentityDto> identities = new ArrayList<>();
		
		for (int index = 0; index < MAX_CREATE; index++) {
			IdmIdentityDto identity = createAndSaveIdentity();
			IdmIdentityContractDto identityContract = createAndSaveIdentityContract(identity, treeNode);
			// provisioning is not executed, role isn't valid from now
			createAndSaveIdentityRole(identityContract, role, null, validFrom);
			identities.add(identity);
		}
		
		list = identityRoleValidRequestService.findAllValid();
		assertEquals(0, list.size());
		
		validFrom = validFrom.minusDays(15);
		for (IdmIdentityDto identity : identities) {
			List<IdmIdentityRole> roles = identityRoleRepository.findAllByIdentityContract_Identity_Id(identity.getId(), null);
			assertEquals(1, roles.size());
			IdmIdentityRole identityRole = roles.get(0);
			identityRole.setValidFrom(validFrom);
			identityRoleRepository.save(identityRole);
		}
		
		list = identityRoleValidRequestService.findAllValid();
		assertEquals(MAX_CREATE, list.size());
		
		IdentityRoleValidRequestTaskExecutor taskExecutor = new IdentityRoleValidRequestTaskExecutor();
		LongRunningFutureTask<Boolean> futureTask = longRunningTaskManager.execute(taskExecutor);
		
		assertEquals(true, futureTask.getFutureTask().get());
		
		IdmLongRunningTaskDto longRunningTask = longRunningTaskService.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		
		list = identityRoleValidRequestService.findAllValid();
		assertEquals(0, list.size());
		
		for (IdmIdentityDto identity : identities) {
			IdentityAccountFilter filter = new IdentityAccountFilter();
			filter.setIdentityId(identity.getId());
			List<AccIdentityAccountDto> accountsList = identityAccountService.find(filter, null).getContent();
			assertEquals(false, accountsList.isEmpty());
			assertEquals(1, accountsList.size());
		}
	}
	
	// TODO: move all these methods higher
	
	private IdmIdentityDto createAndSaveIdentity() {
		IdmIdentityDto entity = new IdmIdentityDto();
		entity.setUsername("valid_identity_" + System.currentTimeMillis());
		entity.setLastName("valid_last_name");
		return saveInTransaction(entity, identityService);
	}
	
	private IdmRoleDto createAndSaveRole() {
		IdmRoleDto entity = new IdmRoleDto();
		entity.setName("valid_role_" + System.currentTimeMillis());
		return saveInTransaction(entity, roleService);
	}
	
	private SysRoleSystem createAndSaveRoleSystem(IdmRoleDto role, SysSystem system) {
		SysRoleSystem entity = new SysRoleSystem();
		entity.setRole(roleRepository.findOne(role.getId()));
		entity.setSystem(system);
		entity.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		return saveInTransaction(entity, sysRoleSystemService);
	}
	
	private IdmTreeType createAndSaveTreeType() {
		IdmTreeType entity = new IdmTreeType();
		entity.setName("valid_tree_type_" + System.currentTimeMillis());
		entity.setCode("valid_tree_type_" + System.currentTimeMillis());
		return saveInTransaction(entity, treeTypeService);
	}
	
	private IdmTreeNode createAndSaveTreeNode(IdmTreeType treeType) {
		IdmTreeNode entity = new IdmTreeNode();
		entity.setCode("valid_tree_node_" + System.currentTimeMillis());
		entity.setName("valid_tree_node_" + System.currentTimeMillis());
		entity.setTreeType(treeType);
		return saveInTransaction(entity, treeNodeService);
	}
	
	private IdmIdentityContractDto createAndSaveIdentityContract(IdmIdentityDto user, IdmTreeNode node) {
		IdmIdentityContractDto entity = new IdmIdentityContractDto();
		entity.setIdentity(user.getId());
		entity.setWorkPosition(node == null ? null : node.getId());
		return saveInTransaction(entity, identityContractService);
	}
	
	private IdmIdentityRoleDto createAndSaveIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role, LocalDate validTill, LocalDate validFrom) {
		IdmIdentityRoleDto entity = new IdmIdentityRoleDto();
		entity.setValidTill(validTill);
		entity.setValidFrom(validFrom);
		entity.setRole(role.getId());
		entity.setIdentityContract(identityContract.getId());
		return saveInTransaction(entity, idmIdentityRoleSerivce);
	}
	
	private SysSystem createAndSaveSystemWithMapping() {
		system = null;
		systemMapping = null;
		SysSystemAttributeMappingDto nameAttributeMapping = null;
		SysSystemAttributeMappingDto firstNameAttributeMapping = null;
		SysSystemAttributeMappingDto lastNameAttributeMapping = null;
		SysSystemAttributeMappingDto passwordAttributeMapping = null;
		// prepare test system
		system = helper.createSystem(TestResource.TABLE_NAME);
		// generate schema
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);
		// create test mapping
		systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0).getId());
		systemMapping = mappingService.save(systemMapping);
		
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for(SysSchemaAttributeDto schemaAttr : schemaAttributesPage) {
			if ("__NAME__".equals(schemaAttr.getName())) {
				nameAttributeMapping = new SysSystemAttributeMappingDto();
				nameAttributeMapping.setUid(true);
				nameAttributeMapping.setEntityAttribute(true);
				nameAttributeMapping.setIdmPropertyName("username");
				nameAttributeMapping.setName(schemaAttr.getName());
				nameAttributeMapping.setSchemaAttribute(schemaAttr.getId());
				nameAttributeMapping.setSystemMapping(systemMapping.getId());
				nameAttributeMapping = attributeMappingService.save(nameAttributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				firstNameAttributeMapping = new SysSystemAttributeMappingDto();
				firstNameAttributeMapping.setIdmPropertyName("firstName");
				firstNameAttributeMapping.setSchemaAttribute(schemaAttr.getId());
				firstNameAttributeMapping.setName(schemaAttr.getName());
				firstNameAttributeMapping.setSystemMapping(systemMapping.getId());
				firstNameAttributeMapping = attributeMappingService.save(firstNameAttributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				lastNameAttributeMapping = new SysSystemAttributeMappingDto();
				lastNameAttributeMapping.setIdmPropertyName("lastName");
				lastNameAttributeMapping.setName(schemaAttr.getName());
				lastNameAttributeMapping.setSchemaAttribute(schemaAttr.getId());
				lastNameAttributeMapping.setSystemMapping(systemMapping.getId());
				lastNameAttributeMapping = attributeMappingService.save(lastNameAttributeMapping);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				passwordAttributeMapping = new SysSystemAttributeMappingDto();
				passwordAttributeMapping.setIdmPropertyName("password");
				passwordAttributeMapping.setSchemaAttribute(schemaAttr.getId());
				passwordAttributeMapping.setName(schemaAttr.getName());
				passwordAttributeMapping.setSystemMapping(systemMapping.getId());
				passwordAttributeMapping = attributeMappingService.save(passwordAttributeMapping);
			}
		}
		assertNotNull(system);
		assertNotNull(nameAttributeMapping);
		assertNotNull(firstNameAttributeMapping);
		assertNotNull(lastNameAttributeMapping);
		assertNotNull(passwordAttributeMapping);
		return system;
	}
}
