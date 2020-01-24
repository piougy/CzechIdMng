package eu.bcvsolutions.idm.acc.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractSliceAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestContractSliceResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractSliceAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.ContractSynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Contract slice synchronization tests
 *
 * @author Svanda
 *
 */
public class ContractSliceSyncTest extends AbstractIntegrationTest {

	private static final String CONTRACT_OWNER_ONE = "contractOwnerOne";
	private static final String CONTRACT_OWNER_TWO = "contractOwnerTwo";
	private static final String CONTRACT_LEADER_ONE = "contractLeaderOne";
	private static final String SYNC_CONFIG_NAME = "syncConfigNameContractSlice";
	private static final String WORK_POSITION_CODE = "workPositionOne";
	private static final String EXTENDED_ATTRIBUTE = "extendedAttribute";

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmContractSliceService contractSliceService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccContractSliceAccountService contractSliceAccountService;
	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmAuditService auditService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysSyncItemLogService syncItemLogService;
	@Autowired
	private SysSyncActionLogService syncActionLogService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		if (identityService.getByUsername(CONTRACT_OWNER_ONE) != null) {
			identityService.delete(identityService.getByUsername(CONTRACT_OWNER_ONE));
		}
		if (identityService.getByUsername(CONTRACT_OWNER_TWO) != null) {
			identityService.delete(identityService.getByUsername(CONTRACT_OWNER_TWO));
		}
		if (identityService.getByUsername(CONTRACT_LEADER_ONE) != null) {
			identityService.delete(identityService.getByUsername(CONTRACT_LEADER_ONE));
		}
		super.logout();
	}

	@Test
	public void createContractSlicesTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmTreeTypeDto treeType = helper.createTreeType();
		IdmTreeNodeDto defaultNode = helper.createTreeNode(treeType, null);

		((SysSyncContractConfigDto) config).setDefaultTreeType(treeType.getId());
		((SysSyncContractConfigDto) config).setDefaultTreeNode(defaultNode.getId());
		config = syncConfigService.save(config);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());
		// Find slice guarantees
		Assert.assertEquals(1, contractSliceManager
				.findSliceGuarantees(contractSliceService.find(contractFilter, null).getContent().get(0).getId())
				.size());

		contractFilter.setValue("2");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());

		contractFilter.setValue("3");
		List<IdmContractSliceDto> contractsThree = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());
		// Find slice guarantees
		Assert.assertEquals(0, contractSliceManager.findSliceGuarantees(contractsThree.get(0).getId()).size());

		contractFilter.setValue("4");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());

		// Delete log
		syncLogService.delete(log);

	}

	/**
	 * Contract slices EAV use definition from the contracts
	 */
	@Test
	public void createContractSlicesEavTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmTreeTypeDto treeType = helper.createTreeType();
		IdmTreeNodeDto defaultNode = helper.createTreeNode(treeType, null);
		helper.createTreeNode(treeType, ContractSliceSyncTest.WORK_POSITION_CODE,
				null);

		((SysSyncContractConfigDto) config).setDefaultTreeType(treeType.getId());
		((SysSyncContractConfigDto) config).setDefaultTreeNode(defaultNode.getId());
		config = syncConfigService.save(config);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmContractSliceDto> slices = contractSliceService.find(contractFilter, null).getContent();

		Assert.assertEquals(1, slices.size());
		IdmContractSliceDto slice = slices.get(0);

		// Contract slices use EAV definition from the contracts
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentityContract.class,
				FormService.DEFAULT_DEFINITION_CODE);

		List<IdmFormValueDto> values = formService.getValues(slice, formDefinition, EXTENDED_ATTRIBUTE);
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(slice.getPosition(), values.get(0).getValue());
		
		// Enable different sync.
		config.setDifferentialSync(true);
		config = syncConfigService.save(config);
		Assert.assertTrue(config.isDifferentialSync());

		// Start sync with enable different sync - no change was made, so only ignore update should be made.
		helper.startSynchronization(config);
		// Three ignored updates
		log = helper.checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 4, OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		// Change EAV slice value
		formService.saveValues(slice, formDefinition, EXTENDED_ATTRIBUTE, Lists.newArrayList(getHelper().createName()));
		
		// Start sync with enable different sync - EAV value changed, so standard ignore update should be made.
		helper.startSynchronization(config);
		log = helper.checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1, OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void deleteSliceAccountTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmContractSliceDto> contractSlices = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractSlices.size());

		// Find the account for this contract slice
		IdmContractSliceDto slice = contractSlices.get(0);
		AccContractSliceAccountFilter contractAccountFilter = new AccContractSliceAccountFilter();
		contractAccountFilter.setSliceId(slice.getId());
		contractAccountFilter.setSystemId(system.getId());
		List<AccContractSliceAccountDto> contractAccounts = contractSliceAccountService
				.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(1, contractAccounts.size());
		AccContractSliceAccountDto contractAccount = contractAccounts.get(0);
		AccAccountDto account = accountService.get(contractAccount.getAccount());
		Assert.assertNotNull(account);

		// Delete this account directly test
		accountService.delete(account);
		account = accountService.get(contractAccount.getAccount());
		Assert.assertNull(account);
		AccContractSliceAccountFilter contractSliceAccountFilter = new AccContractSliceAccountFilter();
		contractSliceAccountFilter.setAccountId(contractAccount.getAccount());
		Assert.assertEquals(0, contractSliceAccountService.find(contractSliceAccountFilter, null).getTotalElements());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void deleteSliceTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmContractSliceDto> contractSlices = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractSlices.size());

		// Find the account for this contract slice
		IdmContractSliceDto slice = contractSlices.get(0);
		AccContractSliceAccountFilter contractAccountFilter = new AccContractSliceAccountFilter();
		contractAccountFilter.setSliceId(slice.getId());
		contractAccountFilter.setSystemId(system.getId());
		List<AccContractSliceAccountDto> contractAccounts = contractSliceAccountService
				.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(1, contractAccounts.size());
		AccContractSliceAccountDto contractAccount = contractAccounts.get(0);
		AccAccountDto account = accountService.get(contractAccount.getAccount());
		Assert.assertNotNull(account);

		// Delete this slice
		contractSliceService.delete(slice);

		contractAccounts = contractSliceAccountService
				.find(contractAccountFilter, null).getContent();
		// Contract - account must be deleted
		Assert.assertEquals(0, contractAccounts.size());
		account = accountService.get(contractAccount.getAccount());
		// Account must be deleted
		Assert.assertNull(account);

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void updateAccountTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);
		config = syncConfigService.save(config);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmContractSliceDto> contractSlices = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractSlices.size());

		// Find the account for this contract slice
		IdmContractSliceDto slice = contractSlices.get(0);
		AccContractSliceAccountFilter contractAccountFilter = new AccContractSliceAccountFilter();
		contractAccountFilter.setSliceId(slice.getId());
		contractAccountFilter.setSystemId(system.getId());
		List<AccContractSliceAccountDto> contractAccounts = contractSliceAccountService
				.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(1, contractAccounts.size());
		AccContractSliceAccountDto contractAccount = contractAccounts.get(0);
		AccAccountDto account = accountService.get(contractAccount.getAccount());
		Assert.assertNotNull(account);

		// Delete log
		syncLogService.delete(log);

		TestContractSliceResource accountOnTargetSystem = this.getBean().findSliceOnTargetSystem("1");
		Assert.assertNull(accountOnTargetSystem.getState());

		// Set slice to disabled
		slice.setState(ContractState.DISABLED);

		// Change settings of sync and run
		config.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ACCOUNT);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		config.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		config = syncConfigService.save(config);
		helper.startSynchronization(config);

		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ACCOUNT, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Sync of slice does not supports provisioning now (account was not changed)!
		accountOnTargetSystem = this.getBean().findSliceOnTargetSystem("1");
		Assert.assertEquals(null, accountOnTargetSystem.getState());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void unlinkAccountTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmContractSliceDto> contractSlices = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractSlices.size());

		// Find the account for this contract slice
		IdmContractSliceDto slice = contractSlices.get(0);
		AccContractSliceAccountFilter contractAccountFilter = new AccContractSliceAccountFilter();
		contractAccountFilter.setSliceId(slice.getId());
		contractAccountFilter.setSystemId(system.getId());
		List<AccContractSliceAccountDto> contractAccounts = contractSliceAccountService
				.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(1, contractAccounts.size());
		AccContractSliceAccountDto contractAccount = contractAccounts.get(0);
		AccAccountDto account = accountService.get(contractAccount.getAccount());
		Assert.assertNotNull(account);

		// Delete log
		syncLogService.delete(log);

		// Change settings of sync and run
		config.setLinkedAction(SynchronizationLinkedActionType.UNLINK);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		config.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		config = syncConfigService.save(config);
		helper.startSynchronization(config);

		log = checkSyncLog(config, SynchronizationActionType.UNLINK, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Find the account for this contract slice ... unlink operation was executed,
		// none relation can be found
		contractAccountFilter = new AccContractSliceAccountFilter();
		contractAccountFilter.setSliceId(slice.getId());
		contractAccountFilter.setSystemId(system.getId());
		contractAccounts = contractSliceAccountService.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(0, contractAccounts.size());
		account = accountService.get(account.getId());
		Assert.assertNull(account);

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void createContractBySliceTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		IdmIdentityDto owner = helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractSliceFilter.setValue("1");
		Assert.assertEquals(1, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		List<IdmContractSliceDto> slicesTwo = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, slicesTwo.size());
		contractSliceFilter.setValue("3");
		List<IdmContractSliceDto> contractsThree = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());
		contractSliceFilter.setValue("4");
		Assert.assertEquals(1, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		// Find created contract
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(owner.getId());
		List<IdmIdentityContractDto> contracts = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(3, contracts.size());
		// Slice with id "2" should be current using
		Assert.assertEquals(1, contracts.stream().filter(c -> c.getPosition().equals("2") && c.isValid()).count());
		Assert.assertTrue(slicesTwo.get(0).isUsingAsContract());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void changeContractBySliceTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		IdmIdentityDto owner = helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractSliceFilter.setValue("1");
		Assert.assertEquals(1, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		List<IdmContractSliceDto> slicesTwo = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, slicesTwo.size());
		contractSliceFilter.setValue("3");
		List<IdmContractSliceDto> contractsThree = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());
		contractSliceFilter.setValue("4");
		Assert.assertEquals(1, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		// Find created contract
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(owner.getId());
		List<IdmIdentityContractDto> contracts = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(3, contracts.size());
		// Slice with id "2" should be current using
		Assert.assertEquals(1, contracts.stream().filter(c -> c.getPosition().equals("2") && c.isValid()).count());
		Assert.assertTrue(slicesTwo.get(0).isUsingAsContract());
		// Delete log
		syncLogService.delete(log);

		// Slice with id "2" reassign from contract ONE to TWO
		this.getBean().changeContractData();

		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Find created contract
		contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(owner.getId());
		contracts = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(3, contracts.size());
		// Slice with id "2" should not be current used
		Assert.assertEquals(0, contracts.stream().filter(c -> c.getPosition().equals("2") && c.isValid()).count());
		// But slice with id "4" should be current used
		Assert.assertEquals(1, contracts.stream().filter(c -> c.getPosition().equals("4") && c.isValid()).count());
		Assert.assertTrue(slicesTwo.get(0).isUsingAsContract());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void sliceWithDefaultPositionTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		IdmTreeTypeDto treeType = helper.createTreeType();
		IdmTreeNodeDto defaultNode = helper.createTreeNode(treeType, null);
		IdmTreeNodeDto workPositionOneNode = helper.createTreeNode(treeType, ContractSliceSyncTest.WORK_POSITION_CODE,
				null);

		((SysSyncContractConfigDto) config).setDefaultTreeType(treeType.getId());
		((SysSyncContractConfigDto) config).setDefaultTreeNode(defaultNode.getId());
		config = syncConfigService.save(config);

		IdmIdentityDto owner = helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractSliceFilter.setValue("1");
		List<IdmContractSliceDto> slicesOne = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, slicesOne.size());
		// Must have sets ContractSliceSyncTest.WORK_POSITION_CODE work position
		Assert.assertEquals(workPositionOneNode.getId(), slicesOne.get(0).getWorkPosition());

		contractSliceFilter.setValue("2");
		List<IdmContractSliceDto> slicesTwo = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, slicesTwo.size());
		// Must have sets default work position
		Assert.assertEquals(defaultNode.getId(), slicesTwo.get(0).getWorkPosition());

		contractSliceFilter.setValue("3");
		List<IdmContractSliceDto> contractsThree = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());
		// Must have sets default work position
		Assert.assertEquals(defaultNode.getId(), contractsThree.get(0).getWorkPosition());

		contractSliceFilter.setValue("4");
		Assert.assertEquals(1, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		// Find created contract
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(owner.getId());
		List<IdmIdentityContractDto> contracts = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(3, contracts.size());
		// Slice with id "2" should be current using and must have work position sets to
		// default node
		Assert.assertEquals(1, contracts.stream().filter(
				c -> c.getPosition().equals("2") && c.isValid() && c.getWorkPosition().equals(defaultNode.getId()))
				.count());
		Assert.assertTrue(slicesTwo.get(0).isUsingAsContract());
		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void sliceWithDefaultLeaderTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		IdmIdentityDto defaultLeader = helper.createIdentity();
		helper.createIdentity(CONTRACT_OWNER_ONE);

		((SysSyncContractConfigDto) config).setDefaultLeader(defaultLeader.getId());
		config = syncConfigService.save(config);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractSliceFilter.setValue("2");
		// Find slice guarantees (Have to be sets default leader.)
		Assert.assertEquals(defaultLeader.getId(), contractSliceManager
				.findSliceGuarantees(contractSliceService.find(contractSliceFilter, null).getContent().get(0).getId())
				.get(0).getGuarantee());
		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void sliceWithNoExistsLeaderTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		// Sync must contains error, because leader CONTRACT_OWNER_ONE does not exist
		Assert.assertTrue(log.isContainsError());
		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void setDirtyStateAndCheckIt() {
		// create tree type and node, tree node is used as position in contrac slice synchronization
		IdmTreeTypeDto treeType = this.getHelper().createTreeType();
		IdmTreeNodeDto treeNode = this.getHelper().createTreeNode(treeType, null);

		// create two roles, one role is used as automatic role by organization structure
		// second role is used as manually added
		IdmRoleDto roleOne = this.getHelper().createRole();
		IdmRoleDto roleTwo = this.getHelper().createRole();
		this.getHelper().createAutomaticRole(roleOne, treeNode);

		// init system
		SysSystemDto system = initData();

		// set default tree type for synchronization
		SysSyncContractConfigDto config = (SysSyncContractConfigDto) doCreateSyncConfig(system);
		config.setDefaultTreeType(treeType.getId());
		syncConfigService.save(config);

		IdmIdentityDto identity = helper.createIdentity();

		// for sure remove all contracts
		contractService.findAllByIdentity(identity.getId()).forEach(contract -> {
			contractService.delete(contract);
		});

		// check current delete audits record for identity (and their related entities)
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		filter.setModification("DELETE");
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(0, audits.size());

		// check current slices
		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setIdentity(identity.getId());
		List<IdmContractSliceDto> slices = contractSliceService.find(contractSliceFilter, null).getContent();
		assertEquals(0, slices.size());

		// check current contracts
		List<IdmIdentityContractDto> allByIdentity = contractService.findAllByIdentity(identity.getId());
		assertEquals(0, allByIdentity.size());

		// delete all data in resource
		this.getBean().deleteAllResourceData();

		// create step one data, please see inside method
		this.getBean().createTestDataStepOne(identity.getUsername(), treeNode.getCode());

		// start synchronization
		helper.startSynchronization(config);
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// after first synchronization exists one contract
		allByIdentity = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, allByIdentity.size());

		// after first synchronization exists one slice
		slices = contractSliceService.find(contractSliceFilter, null).getContent();
		assertEquals(1, slices.size());

		// after first synchronization exists one identity role - automatic role
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(1, identityRoles.size());
		IdmIdentityRoleDto identityRoleDto = identityRoles.get(0);

		// manually create identity account for check if identity account will be changed or deleted after second synchronization
		// this state create two audit records for the identity account
		AccIdentityAccountDto identityAccount = helper.createIdentityAccount(system, identity);
		identityAccount.setIdentityRole(identityRoleDto.getId());
		identityAccount = identityAccountService.save(identityAccount);

		// add manually role
		IdmIdentityContractDto identityContractDto = allByIdentity.get(0);
		this.getHelper().createIdentityRole(identityContractDto, roleTwo);

		// check current identity roles - one is automatic, second is manually added
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());

		// prepare data for second step
		this.getBean().createTestDataStepTwo(identity.getUsername(), treeNode.getCode());
		helper.startSynchronization(config);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// after second synchronization still exists one contract
		allByIdentity = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, allByIdentity.size());

		// after second synchronization exists two slices
		slices = contractSliceService.find(contractSliceFilter, null).getContent();
		assertEquals(2, slices.size());

		// after second synchronization must also exists both roles
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(2, identityRoles.size());

		// check delete operation for identity and their related entities
		filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		filter.setModification("DELETE");
		audits = auditService.find(filter, null).getContent();
		assertEquals(0, audits.size());

		// check audit for identity roles
		for (IdmIdentityRoleDto identityRole : identityRoles) {
			filter = new IdmAuditFilter();
			filter.setEntityId(identityRole.getId());
			List<IdmAuditDto> auditsForIdentityRole = auditService.find(filter, null).getContent();
			if (identityRole.getAutomaticRole() == null) {
				// manually added role, just create
				assertEquals(1, auditsForIdentityRole.size());
			} else {
				// automatic role change validity
				assertEquals(2, auditsForIdentityRole.size());
			}
		}

		// Check audit records for identity account, exists three record, because helper
		// create one and second create save with change identity role and third is
		// delete of this identity-account (role does not mapping the system)
		filter = new IdmAuditFilter();
		filter.setEntityId(identityAccount.getId());
		List<IdmAuditDto> auditsForIdentityAccount = auditService.find(filter, null).getContent();
		assertEquals(3, auditsForIdentityAccount.size());

		// some tests expect data as contract slice with id 1. Just for sure we clear test slices
		slices = contractSliceService.find(contractSliceFilter, null).getContent();
		slices.forEach(slice -> {
			contractSliceService.delete(slice);
		});
		identityService.delete(identity);
	}

	@Test
	public void deleteLastContractSlice() {
		// init system
		SysSystemDto system = initData();
		this.getBean().deleteAllResourceData();

		IdmIdentityDto identity = helper.createIdentity();

		// first expired slice
		this.getBean().createSlice("1", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), LocalDate.now().plusDays(10), LocalDate.now().minusDays(20), "ONE");

		SysSyncContractConfigDto config = (SysSyncContractConfigDto) doCreateSyncConfig(system);
		config.setStartOfHrProcesses(true);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.DELETE_ENTITY);
		config = (SysSyncContractConfigDto) syncConfigService.save(config);
		helper.startSynchronization(config);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> slices = contractSliceService.find(filter, null).getContent();
		assertEquals(1, slices.size());

		IdmContractSliceDto sliceDto = slices.get(0);
		assertEquals(LocalDate.now().minusDays(20), sliceDto.getValidFrom());
		assertEquals(null, sliceDto.getValidTill());

		// create second slice
		this.getBean().createSlice("2", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), LocalDate.now().plusDays(10), LocalDate.now().minusDays(10), "ONE");
		helper.startSynchronization(config);

		slices = contractSliceService.find(filter, null).getContent();
		assertEquals(2, slices.size());

		for (IdmContractSliceDto slice : slices) {
			if ("1".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(20), slice.getValidFrom());
				assertEquals(LocalDate.now().minusDays(10).minusDays(1), slice.getValidTill());
			} else if ("2".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(10), slice.getValidFrom());
				assertEquals(null, slice.getValidTill());
			} else {
				fail("Slice with bad id!");
			}
		}

		this.getBean().deleteSlice("2");

		helper.startSynchronization(config);
		slices = contractSliceService.find(filter, null).getContent();
		assertEquals(1, slices.size());

		sliceDto = slices.get(0);
		assertEquals(LocalDate.now().minusDays(20), sliceDto.getValidFrom());
		assertEquals(null, sliceDto.getValidTill());

		// some tests expect data as contract slice with id 1. Just for sure we clear test slices
		slices = contractSliceService.find(filter, null).getContent();
		slices.forEach(slice -> {
			contractSliceService.delete(slice);
		});
		identityService.delete(identity);
	}

	@Test
	public void addTwoNewAndDeleteFirstContractSliceTurnOffRecalc() {
		// init system
		SysSystemDto system = initData();
		this.getBean().deleteAllResourceData();

		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityContractDto> contracts = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		contractService.delete(contracts.get(0));

		// first valid slice
		this.getBean().createSlice("1", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), null, LocalDate.now().minusDays(20), "ONE");

		SysSyncContractConfigDto config = (SysSyncContractConfigDto) doCreateSyncConfig(system);
		config.setStartOfHrProcesses(true);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.DELETE_ENTITY);
		config = (SysSyncContractConfigDto) syncConfigService.save(config);
		// start sync (recalculation on)
		helper.startSynchronization(config);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> slices = contractSliceService.find(filter, null).getContent();
		assertEquals(1, slices.size());

		IdmContractSliceDto sliceDto = slices.get(0);
		assertEquals(LocalDate.now().minusDays(20), sliceDto.getValidFrom());
		assertEquals(null, sliceDto.getValidTill());

		// create invalid slice
		this.getBean().createSlice("2", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), LocalDate.now().minusDays(10), LocalDate.now().minusDays(10), "ONE");
		// create valid slice
		this.getBean().createSlice("3", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(10), null, LocalDate.now().minusDays(9), "ONE");
		// delete first
		this.getBean().deleteSlice("1");
		// start sync (recalculation off)
		config.setStartOfHrProcesses(false);
		config = (SysSyncContractConfigDto) syncConfigService.save(config);
		helper.startSynchronization(config);

		slices = contractSliceService.find(filter, null).getContent();
		assertEquals(3, slices.size());

		for (IdmContractSliceDto slice : slices) {
			if ("2".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(10), slice.getValidFrom());
				assertEquals(null, slice.getValidTill());
				assertFalse(slice.isUsingAsContract());
				assertNull(slice.getParentContract());
			} else if ("3".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(9), slice.getValidFrom());
				assertEquals(null, slice.getValidTill());
				assertFalse(slice.isUsingAsContract());
				assertNull(slice.getParentContract());
			} else if ("1".equals(slice.getDescription())) {
				// Is not deleted yet
			} else {
				fail("Slice with bad id!");
			}
		}

		contracts = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		IdmIdentityContractDto contract = contracts.get(0);
		assertEquals(LocalDate.now().minusDays(20), contract.getValidFrom());
		assertEquals(null, contract.getValidTill());

		// some tests expect data as contract slice with id 1. Just for sure we clear test slices
		slices = contractSliceService.find(filter, null).getContent();
		slices.forEach(slice -> {
			contractSliceService.delete(slice);
		});
		identityService.delete(identity);
	}

	@Test
	public void addTwoNewAndDeleteFirstContractSlice() {
		// init system
		SysSystemDto system = initData();
		this.getBean().deleteAllResourceData();

		IdmIdentityDto identity = helper.createIdentity();
		List<IdmIdentityContractDto> contracts = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		contractService.delete(contracts.get(0));

		// first valid slice
		this.getBean().createSlice("1", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), null, LocalDate.now().minusDays(20), "ONE");

		SysSyncContractConfigDto config = (SysSyncContractConfigDto) doCreateSyncConfig(system);
		config.setStartOfHrProcesses(true);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.DELETE_ENTITY);
		config = (SysSyncContractConfigDto) syncConfigService.save(config);
		// start sync
		helper.startSynchronization(config);

		IdmContractSliceFilter filter = new IdmContractSliceFilter();
		filter.setIdentity(identity.getId());
		List<IdmContractSliceDto> slices = contractSliceService.find(filter, null).getContent();
		assertEquals(1, slices.size());

		IdmContractSliceDto sliceDto = slices.get(0);
		assertEquals(LocalDate.now().minusDays(20), sliceDto.getValidFrom());
		assertEquals(null, sliceDto.getValidTill());

		// create invalid slice
		this.getBean().createSlice("2", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(20), LocalDate.now().minusDays(10), LocalDate.now().minusDays(10), "ONE");
		// create valid slice
		this.getBean().createSlice("3", identity.getUsername(), null, null, null, null, null, LocalDate.now().minusDays(10), null, LocalDate.now().minusDays(9), "ONE");
		// delete first
		this.getBean().deleteSlice("1");
		// start sync
		helper.startSynchronization(config);

		slices = contractSliceService.find(filter, null).getContent();
		assertEquals(2, slices.size());

		for (IdmContractSliceDto slice : slices) {
			if ("2".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(10), slice.getValidFrom());
				assertEquals(LocalDate.now().minusDays(9).minusDays(1), slice.getValidTill());
				assertNotNull(slice.getParentContract());
			} else if ("3".equals(slice.getDescription())) {
				assertEquals(LocalDate.now().minusDays(9), slice.getValidFrom());
				assertEquals(null, slice.getValidTill());
				assertTrue(slice.isUsingAsContract());
				assertNotNull(slice.getParentContract());
			} else {
				fail("Slice with bad id!");
			}
		}

		contracts = contractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		IdmIdentityContractDto contract = contracts.get(0);
		assertEquals(LocalDate.now().minusDays(10), contract.getValidFrom());
		assertEquals(null, contract.getValidTill());

		// some tests expect data as contract slice with id 1. Just for sure we clear test slices
		slices = contractSliceService.find(filter, null).getContent();
		slices.forEach(slice -> {
			contractSliceService.delete(slice);
		});
		identityService.delete(identity);
	}

	private SysSyncLogDto checkSyncLog(AbstractSysSyncConfigDto config, SynchronizationActionType actionType,
			int count) {
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		List<SysSyncLogDto> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLogDto log = logs.get(0);

		SysSyncActionLogFilter actionLogFilter = new SysSyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLogDto> actions = syncActionLogService.find(actionLogFilter, null).getContent();

		List<SysSyncActionLogDto> actionLogs = actions.stream().filter(action -> {
			return actionType == action.getSyncAction();
		}).collect(Collectors.toList());

		List<SysSyncItemLogDto> result = new ArrayList<>();
		actionLogs.forEach(actionLog -> {
			SysSyncItemLogFilter itemLogFilter = new SysSyncItemLogFilter();
			itemLogFilter.setSyncActionLogId(actionLog.getId());
			List<SysSyncItemLogDto> items = syncItemLogService.find(itemLogFilter, null).getContent();
			result.addAll(items);
		});
		Assert.assertEquals(count, result.size());
		return log;
	}

	public AbstractSysSyncConfigDto doCreateSyncConfig(SysSystemDto system) {

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.CONTRACT_SLICE);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto uidAttribute = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).findFirst().orElse(null);

		// Create default synchronization config
		AbstractSysSyncConfigDto syncConfigCustom = new SysSyncContractConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(false);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(uidAttribute.getId());
		syncConfigCustom.setName(SYNC_CONFIG_NAME);
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigCustom = syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		return syncConfigCustom;
	}

	private TestContractSliceResource createContract(String code, String owner, String leader, String main,
			String workposition, String state, String disabled, LocalDate validFromContract,
			LocalDate validTillContract, LocalDate validFromSlice, String contractCode) {
		TestContractSliceResource contract = new TestContractSliceResource();
		contract.setId(code);
		contract.setName(code);
		contract.setOwner(owner);
		contract.setState(state);
		contract.setDisabled(disabled);
		contract.setLeader(leader);
		contract.setMain(main);
		contract.setWorkposition(workposition);
		contract.setDescription(code);
		contract.setValidFrom(validFromContract);
		contract.setValidTill(validTillContract);
		contract.setValidFromSlice(validFromSlice);
		contract.setContractCode(contractCode);
		return contract;
	}

	private SysSystemDto initData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestContractSliceResource.TABLE_NAME, null, null, "ID");
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.CONTRACT_SLICE);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		this.getBean().initContractData();
		return system;

	}

	@Transactional
	public void createContractData(String code, String owner, String leader, String main, String workposition,
			String state, String disabled, LocalDate validFromContract, LocalDate validTillContract,
			LocalDate validFromSlice, String contractCode) {
		if (code == null) {
			code = String.valueOf(System.currentTimeMillis());
		}
		entityManager.persist(this.createContract(code, owner, leader, main, workposition, state, disabled,
				validFromContract, validTillContract, validFromSlice, contractCode));
	}

	@Transactional
	public void initContractData() {
		deleteAllResourceData();
		entityManager.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true",
				ContractSliceSyncTest.WORK_POSITION_CODE, null, null, null, null, LocalDate.now().minusDays(10),
				"ONE"));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, null, null, null, null,
				LocalDate.now().minusDays(1), "ONE"));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_ONE, null, "true", null, null, null, null, null,
				LocalDate.now().plusDays(10), "ONE"));
		entityManager.persist(this.createContract("4", CONTRACT_OWNER_ONE, null, "true", null, null, null, null, null,
				LocalDate.now(), "TWO"));

	}

	@Transactional
	public void changeContractData() {
		deleteAllResourceData();
		entityManager.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", null, null,
				null, null, null, LocalDate.now().minusDays(10), "ONE"));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, null, null, null, null,
				LocalDate.now().minusDays(1), "TWO"));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_ONE, null, "true", null, null, null, null, null,
				LocalDate.now().plusDays(10), "ONE"));
		entityManager.persist(this.createContract("4", CONTRACT_OWNER_ONE, null, "true", null, null, null, null, null,
				LocalDate.now(), "TWO"));

	}

	@Transactional
	public void createTestDataStepOne(String owner, String treeNodeCode) {
		entityManager.persist(this.createContract("1", owner, null, "true", treeNodeCode, null, null, LocalDate.now().minusDays(10), null, LocalDate.now().minusDays(7), "ONE"));
	}

	@Transactional
	public void createTestDataStepTwo(String owner, String treeNodeCode) {
		TestContractSliceResource sliceOne = entityManager.find(TestContractSliceResource.class, "1");
		sliceOne.setValidTill(LocalDate.now().minusDays(7));
		entityManager.persist(this.createContract("2", owner, null, "true", treeNodeCode, null, null, LocalDate.now().minusDays(4), null, LocalDate.now().minusDays(5), "ONE"));
	}

	@Transactional
	public void createSlice(String code, String owner, String leader, String main,
			String workposition, String state, String disabled, LocalDate validFromContract,
			LocalDate validTillContract, LocalDate validFromSlice, String contractCode) {
		entityManager.persist(this.createContract(code, owner, leader, main, workposition, state, disabled, validFromContract, validTillContract, validFromSlice, contractCode));
	}

	@Transactional
	public void deleteSlice(String id) {
		TestContractSliceResource slice = entityManager.find(TestContractSliceResource.class, id);
		entityManager.remove(slice);
	}

	@Transactional
	public TestContractSliceResource findSliceOnTargetSystem(String uid) {
		return entityManager.find(TestContractSliceResource.class, uid);

	}

	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("id".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setIdmPropertyName(IdmIdentityContract_.description.getName()); // it is for link
																										// and update
																										// situation
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("name".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("position");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("owner".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_IDENTITY_FIELD);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("workposition".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_WORK_POSITION_FIELD);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("state".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_STATE_FIELD);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setTransformFromResourceScript(
						"return scriptEvaluator.evaluate(\n" + "    scriptEvaluator.newBuilder()\n"
								+ "        .setScriptCode('compileIdentityRelationState')\n"
								+ "        .addParameter('scriptEvaluator', scriptEvaluator)\n"
								+ "        .addParameter('attributeValue', attributeValue)\n"
								+ "        .addParameter('icAttributes', icAttributes)\n"
								+ "        .addParameter('system', system)\n" + "	.build());");
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("leader".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_GUARANTEES_FIELD);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("modified".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			} else if ("validfrom".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName(IdmContractSlice_.contractValidFrom.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("validtill".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName(IdmContractSlice_.contractValidTill.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			} else if ("validfrom_slice".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName(IdmContractSlice_.validFrom.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("description".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto extendedAttribute = new SysSystemAttributeMappingDto();
				extendedAttribute.setUid(false);
				extendedAttribute.setEntityAttribute(false);
				extendedAttribute.setExtendedAttribute(true);
				extendedAttribute.setName(EXTENDED_ATTRIBUTE);
				extendedAttribute.setSchemaAttribute(schemaAttr.getId());
				extendedAttribute.setSystemMapping(entityHandlingResult.getId());
				extendedAttribute.setIdmPropertyName(EXTENDED_ATTRIBUTE);
				schemaAttributeMappingService.save(extendedAttribute);

			} else if ("contract_code".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(IdmContractSlice_.contractCode.getName());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			}
		});
	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestContractSliceResource.TABLE_NAME);
		q.executeUpdate();
	}

	private ContractSliceSyncTest getBean() {
		return applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass());
	}
}
