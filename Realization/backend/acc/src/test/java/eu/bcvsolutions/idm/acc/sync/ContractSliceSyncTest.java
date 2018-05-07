package eu.bcvsolutions.idm.acc.sync;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.TestHelper;
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
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.ContractSynchronizationExecutor;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSynchronizationService;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Contract slice synchronization tests
 * 
 * @author Svanda
 *
 */
@Service
public class ContractSliceSyncTest extends AbstractIntegrationTest {

	private static final String CONTRACT_OWNER_ONE = "contractOwnerOne";
	private static final String CONTRACT_OWNER_TWO = "contractOwnerTwo";
	private static final String CONTRACT_LEADER_ONE = "contractLeaderOne";
	private static final String SYNC_CONFIG_NAME = "syncConfigNameContractSlice";
	private static final String WORK_POSITION_CODE = "workPositionOne";

	@Autowired
	private TestHelper helper;
	@Autowired
	private ApplicationContext context;
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
	private SysSyncItemLogService syncItemLogService;
	@Autowired
	private SysSyncActionLogService syncActionLogService;
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

	private SynchronizationService synchornizationService;

	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
		synchornizationService = context.getAutowireCapableBeanFactory()
				.createBean(DefaultSynchronizationService.class);
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

		IdmContractSliceFilter contractFilter = new IdmContractSliceFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractFilter, null).getTotalElements());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());
		// Find slice guarantees
		Assert.assertEquals(1, contractSliceManager
				.findSliceGuarantees(contractSliceService.find(contractFilter, null).getContent().get(0).getId()));

		contractFilter.setValue("2");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());

		contractFilter.setValue("3");
		List<IdmContractSliceDto> contractsThree = contractSliceService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());
		// Find slice guarantees
		Assert.assertEquals(0, contractSliceManager.findSliceGuarantees(contractsThree.get(0).getId()));

		contractFilter.setValue("4");
		Assert.assertEquals(1, contractSliceService.find(contractFilter, null).getTotalElements());

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

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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
	public void updateAccountTest() {
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

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

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

		synchornizationService.process();

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

		IdmIdentityDto owner = helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setProperty(IdmIdentityContract_.position.getName());
		contractSliceFilter.setValue("1");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());
		contractSliceFilter.setValue("2");
		Assert.assertEquals(0, contractSliceService.find(contractSliceFilter, null).getTotalElements());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 4);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractSliceFilter.setValue("1");
		List<IdmContractSliceDto> slicesOne = contractSliceService.find(contractSliceFilter, null).getContent();
		Assert.assertEquals(1, slicesOne);
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
		// ContractSliceSyncTest.WORK_POSITION_CODE
		Assert.assertEquals(1, contracts.stream().filter(c -> c.getPosition().equals("2") && c.isValid()
				&& c.getWorkPosition().equals(workPositionOneNode.getId())).count());
		Assert.assertTrue(slicesTwo.get(0).isUsingAsContract());
		// Delete log
		syncLogService.delete(log);

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

		SysSyncActionLogDto actionLog = actions.stream().filter(action -> {
			return actionType == action.getSyncAction();
		}).findFirst().get();

		SysSyncItemLogFilter itemLogFilter = new SysSyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLogDto> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(count, items.size());
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
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("description");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
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
		return applicationContext.getBean(this.getClass());
	}
}
