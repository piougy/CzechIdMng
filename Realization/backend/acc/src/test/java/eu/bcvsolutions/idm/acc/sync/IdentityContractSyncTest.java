package eu.bcvsolutions.idm.acc.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccContractAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestContractResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
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
import eu.bcvsolutions.idm.acc.test.TestHelper;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrContractExclusionProcess;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEnableContractProcess;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEndContractProcess;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Contract (identity relation) synchronization tests
 * 
 * @author Svanda
 *
 */
@Service
public class IdentityContractSyncTest extends AbstractIntegrationTest {

	private static final String CONTRACT_OWNER_ONE = "contractOwnerOne";
	private static final String CONTRACT_OWNER_TWO = "contractOwnerTwo";
	private static final String CONTRACT_LEADER_ONE = "contractLeaderOne";
	private static final String CONTRACT_LEADER_TWO = "contractLeaderTwo";
	private static final String SYNC_CONFIG_NAME = "syncConfigNameContract";

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
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractGuaranteeService guaranteeService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private SchedulerManager schedulerService;
	@Autowired
	private IdmScheduledTaskService scheduledService;
	@Autowired
	private AccContractAccountService contractAccountService;
	@Autowired
	private AccAccountService accountService;

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
		if (identityService.getByUsername(CONTRACT_LEADER_TWO) != null) {
			identityService.delete(identityService.getByUsername(CONTRACT_LEADER_TWO));
		}
		super.logout();
	}

	@Test
	public void createContractTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		Assert.assertEquals(1, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getState());

		// Delete log
		syncLogService.delete(log);

	}
	
	@Test
	public void deleteContractAccountTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contracts = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contracts.size());
		
		// Find the account for this contract
		IdmIdentityContractDto contract = contracts.get(0);
		AccContractAccountFilter contractAccountFilter = new AccContractAccountFilter();
		contractAccountFilter.setContractId(contract.getId());
		contractAccountFilter.setSystemId(system.getId());
		List<AccContractAccountDto> contractAccounts = contractAccountService.find(contractAccountFilter, null).getContent();
		Assert.assertEquals(1, contractAccounts.size());
		AccContractAccountDto contractAccount = contractAccounts.get(0);
		AccAccountDto account = accountService.get(contractAccount.getAccount());
		Assert.assertNotNull(account);
		
		// Delete this account directly test
		accountService.delete(account);
		account = accountService.get(contractAccount.getAccount());
		Assert.assertNull(account);

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void checkContractExcludeTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		// Change resources (set state on exclude) .. must be call in
		// transaction
		this.getBean().initContractCheckExcludeTest();

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		Assert.assertEquals(1, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		Assert.assertEquals(null, contractsTwo.get(0).getState());
		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(ContractState.EXCLUDED, contractsThree.get(0).getState());

		// Delete log
		syncLogService.delete(log);

	}

	@Transactional
	public void initContractCheckExcludeTest() {
		deleteAllResourceData();
		entityManager
				.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", null, null, null));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, "40", null));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_TWO, null, "true", null, "10", null));

	}

	@Test
	public void checkContractDisableTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		// Change resources (set state on disable) .. must be call in
		// transaction
		this.getBean().initContractCheckDisableTest();

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		Assert.assertEquals(ContractState.DISABLED, contractsOne.get(0).getState());
		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		Assert.assertEquals(null, contractsTwo.get(0).getState());
		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(ContractState.DISABLED, contractsThree.get(0).getState());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	/**
	 * HR process are not executed during sync. If contract is invalid, then HR
	 * process disable the Identity. But in the sync we need skip this
	 * functionality.
	 */
	public void checkContractInvalidTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);
		((SysSyncContractConfigDto) config).setStartOfHrProcesses(false);
		syncConfigService.save(config);

		IdmIdentityDto ownerOne = helper.createIdentity(CONTRACT_OWNER_ONE);
		IdmIdentityDto ownerTwo = helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);
		contractService.findAllByIdentity(ownerOne.getId()).forEach(contract -> {
			IdentityContractEvent event = new IdentityContractEvent(IdentityContractEventType.DELETE, contract);
			event.getProperties().put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			contractService.publish(event);
		});
		contractService.findAllByIdentity(ownerTwo.getId()).forEach(contract -> {
			IdentityContractEvent event = new IdentityContractEvent(IdentityContractEventType.DELETE, contract);
			event.getProperties().put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			contractService.publish(event);
		});

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		// Change resources (set to invalid) .. must be call in transaction
		this.getBean().initContractCheckInvalidTest();

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		Assert.assertFalse(contractsOne.get(0).isValid());
		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertTrue(contractsThree.get(0).isValid());

		// HR processes was not started, identity have to be in "incorrect" state
		ownerOne = identityService.getByUsername(CONTRACT_OWNER_ONE);
		Assert.assertFalse(ownerOne.isDisabled());
		ownerTwo = identityService.getByUsername(CONTRACT_OWNER_TWO);
		Assert.assertFalse(ownerTwo.isDisabled());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	/**
	 * HR process are not executed during sync, but after sync end.
	 */
	public void checkContractInvalidWithStartHrProcessesTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);
		((SysSyncContractConfigDto) config).setStartOfHrProcesses(true);
		syncConfigService.save(config);

		Task task = findTask(HrEnableContractProcess.class);
		IdmScheduledTaskDto scheduledTask = null;
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}

		task = findTask(HrEndContractProcess.class);
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}

		task = findTask(HrContractExclusionProcess.class);
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}

		IdmIdentityDto ownerOne = helper.createIdentity(CONTRACT_OWNER_ONE);
		IdmIdentityDto ownerTwo = helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);
		contractService.findAllByIdentity(ownerOne.getId()).forEach(contract -> {
			IdentityContractEvent event = new IdentityContractEvent(IdentityContractEventType.DELETE, contract);
			event.getProperties().put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			contractService.publish(event);
		});
		contractService.findAllByIdentity(ownerTwo.getId()).forEach(contract -> {
			IdentityContractEvent event = new IdentityContractEvent(IdentityContractEventType.DELETE, contract);
			event.getProperties().put(IdmIdentityContractService.SKIP_HR_PROCESSES, Boolean.TRUE);
			contractService.publish(event);
		});

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		// Change resources (set to invalid) .. must be call in transaction
		this.getBean().initContractCheckInvalidTest();

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		Assert.assertFalse(contractsOne.get(0).isValid());
		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertTrue(contractsThree.get(0).isValid());

		// HR processes was started, identity have to be in "correct" state
		ownerOne = identityService.getByUsername(CONTRACT_OWNER_ONE);
		Assert.assertTrue(ownerOne.isDisabled());
		ownerTwo = identityService.getByUsername(CONTRACT_OWNER_TWO);
		Assert.assertFalse(ownerTwo.isDisabled());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void defaultLeaderTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);
		IdmIdentityDto defaultLeader = helper.createIdentity(CONTRACT_LEADER_TWO);

		// Set default leader to sync configuration
		SysSyncContractConfigDto configContract = (SysSyncContractConfigDto) config;
		configContract.setDefaultLeader(defaultLeader.getId());
		config = syncConfigService.save(configContract);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());
		contractFilter.setValue("1");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());
		contractFilter.setValue("2");
		Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		IdmContractGuaranteeFilter guaranteeFilter = new IdmContractGuaranteeFilter();
		guaranteeFilter.setIdentityContractId(contractsOne.get(0).getId());
		List<IdmContractGuaranteeDto> gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		Assert.assertEquals(1, gurantees.size());
		IdmIdentityDto guarantee = DtoUtils.getEmbedded(gurantees.get(0), IdmContractGuarantee_.guarantee,
				IdmIdentityDto.class);
		// Direct leader from resource
		Assert.assertEquals(CONTRACT_LEADER_ONE, guarantee.getUsername());

		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		guaranteeFilter.setIdentityContractId(contractsTwo.get(0).getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		Assert.assertEquals(1, gurantees.size());
		guarantee = DtoUtils.getEmbedded(gurantees.get(0), IdmContractGuarantee_.guarantee, IdmIdentityDto.class);
		// Default leader
		Assert.assertEquals(CONTRACT_LEADER_TWO, guarantee.getUsername());

		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		guaranteeFilter.setIdentityContractId(contractsThree.get(0).getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		Assert.assertEquals(1, gurantees.size());
		guarantee = DtoUtils.getEmbedded(gurantees.get(0), IdmContractGuarantee_.guarantee, IdmIdentityDto.class);
		// Default leader
		Assert.assertEquals(CONTRACT_LEADER_TWO, guarantee.getUsername());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void defaultTreeTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);
		helper.createIdentity(CONTRACT_LEADER_TWO);

		// Set default tree type to sync configuration
		IdmTreeTypeDto treeType = treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE);
		Assert.assertNotNull(treeType);
		SysSyncContractConfigDto configContract = (SysSyncContractConfigDto) config;
		configContract.setDefaultTreeType(treeType.getId());
		config = syncConfigService.save(configContract);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());

		// Start sync
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// None work positions can be found

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		Assert.assertEquals(null, contractsOne.get(0).getWorkPosition());

		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		Assert.assertEquals(null, contractsTwo.get(0).getWorkPosition());

		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getWorkPosition());

		// Delete log
		syncLogService.delete(log);

		// Set work positions to resources
		this.getBean().initContractDefaultTreeTest();

		// Start sync again (we want to see some work positions)
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// For contract One must be found workposition (one)
		contractFilter.setValue("1");
		contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		IdmTreeNodeDto workposition = DtoUtils.getEmbedded(contractsOne.get(0), IdmIdentityContract_.workPosition,
				IdmTreeNodeDto.class);
		Assert.assertEquals("one", workposition.getCode());

		// For contract Two must not be found workposition (WRONG node is not in
		// default
		// tree)
		contractFilter.setValue("2");
		contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		Assert.assertEquals(null, contractsTwo.get(0).getWorkPosition());

		contractFilter.setValue("3");
		contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(null, contractsThree.get(0).getWorkPosition());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void defaultWorkPositionTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		Assert.assertTrue(config instanceof SysSyncContractConfigDto);

		helper.createIdentity(CONTRACT_OWNER_ONE);
		helper.createIdentity(CONTRACT_OWNER_TWO);
		helper.createIdentity(CONTRACT_LEADER_ONE);
		helper.createIdentity(CONTRACT_LEADER_TWO);

		// Set default tree type to sync configuration
		IdmTreeTypeDto treeType = treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE);
		Assert.assertNotNull(treeType);
		SysSyncContractConfigDto configContract = (SysSyncContractConfigDto) config;
		configContract.setDefaultTreeType(treeType.getId());

		// Set default tree node to sync configuration
		IdmTreeNodeFilter nodeFilter = new IdmTreeNodeFilter();
		nodeFilter.setCode("one");
		nodeFilter.setTreeTypeId(treeType.getId());
		List<IdmTreeNodeDto> nodes = treeNodeService.find(nodeFilter, null).getContent();
		Assert.assertEquals(1, nodes.size());
		IdmTreeNodeDto defaultNode = nodes.get(0);
		configContract.setDefaultTreeNode(defaultNode.getId());
		config = syncConfigService.save(configContract);

		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setProperty(IdmIdentityContract_.position.getName());

		// Start sync
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Default work positions must be set

		contractFilter.setValue("1");
		List<IdmIdentityContractDto> contractsOne = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsOne.size());
		Assert.assertEquals(defaultNode.getId(), contractsOne.get(0).getWorkPosition());

		contractFilter.setValue("2");
		List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsTwo.size());
		Assert.assertEquals(defaultNode.getId(), contractsTwo.get(0).getWorkPosition());

		contractFilter.setValue("3");
		List<IdmIdentityContractDto> contractsThree = contractService.find(contractFilter, null).getContent();
		Assert.assertEquals(1, contractsThree.size());
		Assert.assertEquals(defaultNode.getId(), contractsThree.get(0).getWorkPosition());

		// Delete log
		syncLogService.delete(log);

	}
	
	@Test
	public void testLinkAndUpdateContract() {
		String position1 = "test-link-update-1-" + System.currentTimeMillis();
		String position2 = "test-link-update-2-" + System.currentTimeMillis();
		String position3 = "test-link-update-3-" + System.currentTimeMillis();
		
		IdmIdentityDto leader = helper.createIdentity();
		IdmTreeNodeDto workPosition = helper.createTreeNode();
		
		SysSystemDto system = initData();
		AbstractSysSyncConfigDto config = doCreateSyncConfig(system);
		
		this.getBean().deleteAllResourceData();
		
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		config = (SysSyncContractConfigDto) syncConfigService.save(config);
		
		IdmIdentityDto identity1 = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		IdmIdentityDto identity3 = helper.createIdentity();
		
		IdmIdentityContractDto contrac1 = helper.getPrimeContract(identity1.getId());
		IdmIdentityContractDto contrac2 = helper.getPrimeContract(identity2.getId());
		IdmIdentityContractDto contrac3 = helper.getPrimeContract(identity3.getId());
		
		contrac1.setPosition(position1);
		contrac1.setDescription(position1);
		contrac2.setPosition(position2);
		contrac2.setDescription(position2);
		contrac3.setPosition(position3);
		contrac3.setDescription(position3);

		contrac1 = contractService.save(contrac1);
		contrac2 = contractService.save(contrac2);
		contrac3 = contractService.save(contrac3);
		
		// check empty guarantee
		IdmContractGuaranteeFilter guaranteeFilter = new IdmContractGuaranteeFilter();
		guaranteeFilter.setIdentityContractId(contrac1.getId());
		List<IdmContractGuaranteeDto> gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertTrue(gurantees.isEmpty());
		
		guaranteeFilter.setIdentityContractId(contrac2.getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertTrue(gurantees.isEmpty());
		
		guaranteeFilter.setIdentityContractId(contrac3.getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertTrue(gurantees.isEmpty());
		
		assertNull(contrac1.getState());
		assertNull(contrac2.getState());
		assertNull(contrac3.getState());
		
		this.getBean().createContractData(position1, identity1.getUsername(), leader.getUsername(), Boolean.TRUE.toString(), workPosition.getId().toString(), "10", Boolean.FALSE.toString());
		this.getBean().createContractData(position2, identity2.getUsername(), leader.getUsername(), Boolean.TRUE.toString(), workPosition.getId().toString(), "10", Boolean.FALSE.toString());
		this.getBean().createContractData(position3, identity3.getUsername(), leader.getUsername(), Boolean.TRUE.toString(), workPosition.getId().toString(), "10", Boolean.FALSE.toString());

		// Start sync
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		contractService.findAllByIdentity(identity1.getId());

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 3);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		IdmIdentityContractDto updatedContract1 = helper.getPrimeContract(identity1.getId());
		IdmIdentityContractDto updatedContract2 = helper.getPrimeContract(identity2.getId());
		IdmIdentityContractDto updatedContract3 = helper.getPrimeContract(identity3.getId());
		
		assertNotEquals(updatedContract1.getModified(), contrac1.getModified());
		assertNotEquals(updatedContract2.getModified(), contrac2.getModified());
		assertNotEquals(updatedContract3.getModified(), contrac3.getModified());
		
		assertNotEquals(updatedContract1.getState(), contrac1.getState());
		assertNotEquals(updatedContract2.getState(), contrac2.getState());
		assertNotEquals(updatedContract3.getState(), contrac3.getState());
		
		assertEquals(ContractState.EXCLUDED, updatedContract1.getState());
		assertEquals(ContractState.EXCLUDED, updatedContract2.getState());
		assertEquals(ContractState.EXCLUDED, updatedContract3.getState());
		
		assertEquals(contrac1.getId(), updatedContract1.getId());
		assertEquals(contrac2.getId(), updatedContract2.getId());
		assertEquals(contrac3.getId(), updatedContract3.getId());
		
		guaranteeFilter.setIdentityContractId(contrac1.getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertFalse(gurantees.isEmpty());
		assertEquals(leader.getId(), gurantees.get(0).getGuarantee());
		
		guaranteeFilter.setIdentityContractId(contrac2.getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertFalse(gurantees.isEmpty());
		assertEquals(leader.getId(), gurantees.get(0).getGuarantee());
		
		guaranteeFilter.setIdentityContractId(contrac3.getId());
		gurantees = guaranteeService.find(guaranteeFilter, null).getContent();
		assertFalse(gurantees.isEmpty());
		assertEquals(leader.getId(), gurantees.get(0).getGuarantee());
	}

	@Transactional
	public void initContractDefaultTreeTest() {
		deleteAllResourceData();
		entityManager
				.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", "one", null, null));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, null, null));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_TWO, null, "true", null, null, null));

	}

	@Transactional
	public void initContractCheckDisableTest() {
		deleteAllResourceData();
		entityManager
				.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", null, null, "true"));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, "40", "false"));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_TWO, null, "true", null, "10", "true"));

	}

	@Transactional
	public void initContractCheckInvalidTest() {
		deleteAllResourceData();
		TestContractResource one = this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", null, null,
				null);
		one.setValidFrom(LocalDate.now().plusDays(1));
		entityManager.persist(one);
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_TWO, null, "true", null, null, "false"));

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
		mappingFilter.setEntityType(SystemEntityType.CONTRACT);
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

	private TestContractResource createContract(String code, String owner, String leader, String main,
			String workposition, String state, String disabled) {
		TestContractResource contract = new TestContractResource();
		contract.setId(code);
		contract.setName(code);
		contract.setOwner(owner);
		contract.setState(state);
		contract.setDisabled(disabled);
		contract.setLeader(leader);
		contract.setMain(main);
		contract.setWorkposition(workposition);
		contract.setDescription(code);
		return contract;
	}

	private SysSystemDto initData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestContractResource.TABLE_NAME, null, null, "ID");
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.CONTRACT);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		this.getBean().initContractData();
		return system;

	}
	
	@Transactional
	public void createContractData(String code, String owner, String leader, String main, 
			String workposition, String state, String disabled) {
		if (code == null) {
			code = String.valueOf(System.currentTimeMillis());
		}
		entityManager.persist(this.createContract(code, owner, leader, main, workposition, state, disabled));
	}

	@Transactional
	public void initContractData() {
		deleteAllResourceData();
		entityManager
				.persist(this.createContract("1", CONTRACT_OWNER_ONE, CONTRACT_LEADER_ONE, "true", null, null, null));
		entityManager.persist(this.createContract("2", CONTRACT_OWNER_ONE, null, "false", null, null, null));
		entityManager.persist(this.createContract("3", CONTRACT_OWNER_TWO, null, "true", null, null, null));

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
				attributeHandlingName.setIdmPropertyName(IdmIdentityContract_.description.getName()); // it is for link and update situation
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
				attributeHandlingName.setIdmPropertyName("validFrom");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setTransformFromResourceScript(
						"return attributeValue == null ? null : org.joda.time.LocalDate.parse(attributeValue);");
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("validtill".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName("validTill");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setTransformFromResourceScript(
						"return attributeValue == null ? null : org.joda.time.LocalDate.parse(attributeValue);");
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("description".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("description");
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
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestContractResource.TABLE_NAME);
		q.executeUpdate();
	}

	private IdentityContractSyncTest getBean() {
		return applicationContext.getBean(this.getClass());
	}

	private Task findTask(Class<? extends SchedulableTaskExecutor<?>> taskType) {
		List<Task> tasks = schedulerService.getAllTasksByType(taskType);
		if (tasks.size() == 1) {
			return tasks.get(0);
		}
		if (tasks.isEmpty()) {
			return null;
		}

		Task defaultTask = tasks.stream().filter(task -> {
			return task.getDescription().equals("Default");
		}).findFirst().orElse(null);
		if (defaultTask != null) {
			return defaultTask;
		}
		return tasks.get(0);
	}
}
