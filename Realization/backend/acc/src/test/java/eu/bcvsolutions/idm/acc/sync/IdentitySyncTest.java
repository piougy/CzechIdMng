package eu.bcvsolutions.idm.acc.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSynchronizationService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSynchronizationServiceTest;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Identity synchronization tests (basic tests for identity sync are in
 * {@link DefaultSynchronizationServiceTest})
 * 
 * @author Svanda
 *
 */
@Service
public class IdentitySyncTest extends AbstractIntegrationTest {

	private static final String IDENTITY_ONE = "identityOne";
	private static final String SYNC_CONFIG_NAME = "syncConfigNameContract";
	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String ATTRIBUTE_EMAIL = "email";

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
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmScriptAuthorityService scriptAuthrotityService;
	@Autowired
	private IdmScriptService scriptService;
	@Autowired
	private TestIdentityProcessor testIdentityProcessor;
	
	private SynchronizationService synchornizationService;

	@Before
	public void init() {
		loginAsAdmin("admin");
		synchornizationService = context.getAutowireCapableBeanFactory()
				.createBean(DefaultSynchronizationService.class);
	}

	@After
	public void logout() {
		if (identityService.getByUsername(IDENTITY_ONE) != null) {
			identityService.delete(identityService.getByUsername(IDENTITY_ONE));
		}
		super.logout();
	}

	@Test
	public void createIdentityWithDefaultRoleTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		// Have to be in the warning state, because default role cannot be assigned for
		// new identity, because sync do not creates the default contract. See
		// IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION.
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
		Assert.assertEquals(0, roles.size());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void updateIdentityWithDefaultRoleTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(1, roles.size());
		Assert.assertEquals(defaultRole.getId(), roles.get(0).getRole());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void updateIdentityWithInvalidContractTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityOne.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(LocalDate.now().minusDays(10));
		primeContract = contractService.save(primeContract);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(0, roles.size());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void updateIdentityPropagateValidityTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityOne.getId());
		Assert.assertNotNull(primeContract);

		LocalDate validTill = LocalDate.now().plusDays(10);
		LocalDate validFrom = LocalDate.now().plusDays(-10);
		primeContract.setValidFrom(validFrom);
		primeContract.setValidTill(validTill);
		primeContract = contractService.save(primeContract);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto identityRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), identityRole.getRole());
		Assert.assertEquals(identityRole.getValidFrom(), validFrom);
		Assert.assertEquals(identityRole.getValidTill(), validTill);

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(identityRole.getId());
		Assert.assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void deleteDefaulRoleIntegrityTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		Assert.assertNotNull(config.getDefaultRole());
		// Delete default role
		roleService.delete(defaultRole);
		config = (SysSyncIdentityConfigDto) syncConfigService.get(config.getId());
		Assert.assertNull(config.getDefaultRole());
	}
	
	@Test
	public void testSynchronizationCache() {
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		this.getBean().deleteAllResourceData();
		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";
		
		String userOne = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(userOne, testFirstName, testLastName);
		
		String userTwo = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(userTwo, testFirstName, testLastName);
		
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.IDENTITY);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto defaultMapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(defaultMapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		
		SysSystemAttributeMappingDto firstNameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getIdmPropertyName().equals(IdmIdentity_.firstName.getName());
		}).findFirst().orElse(null);
		
		Assert.assertNotNull(firstNameAttribute);
		StringBuilder scriptGenerateUuid = new StringBuilder();
		scriptGenerateUuid.append("import java.util.UUID;");
		scriptGenerateUuid.append(System.lineSeparator());
		scriptGenerateUuid.append("return UUID.randomUUID();");
		
		String scriptName = "generateUuid";
		IdmScriptDto scriptUuid = new IdmScriptDto();
		scriptUuid.setCategory(IdmScriptCategory.TRANSFORM_FROM);
		scriptUuid.setCode(scriptName);
		scriptUuid.setName(scriptName);
		scriptUuid.setScript(scriptGenerateUuid.toString());
		scriptUuid = scriptService.save(scriptUuid);
		
		IdmScriptAuthorityDto scriptAuth = new IdmScriptAuthorityDto();
		scriptAuth.setClassName("java.util.UUID");
		scriptAuth.setType(ScriptAuthorityType.CLASS_NAME);
		scriptAuth.setScript(scriptUuid.getId());
		scriptAuth = scriptAuthrotityService.save(scriptAuth);
		
		// we must call script
		StringBuilder transformationScript = new StringBuilder();
		
		transformationScript.append("return scriptEvaluator.evaluate(");
		transformationScript.append(System.lineSeparator());
		transformationScript.append("scriptEvaluator.newBuilder()");
		transformationScript.append(System.lineSeparator());
		transformationScript.append(".setScriptCode('" + scriptName + "')");
		transformationScript.append(System.lineSeparator());
		transformationScript.append(".build());");
		transformationScript.append(System.lineSeparator());
		
		firstNameAttribute.setTransformFromResourceScript(transformationScript.toString());
		firstNameAttribute.setCached(true);
		firstNameAttribute = schemaAttributeMappingService.save(firstNameAttribute);
		
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2,
				OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();
		assertEquals(2, identities.size());
		//
		IdmIdentityDto identityOne = identities.get(0);
		IdmIdentityDto identityTwo = identities.get(1);
		//
		assertNotEquals(identityOne.getFirstName(), identityTwo.getFirstName());
	}
	
	@Test
	public void testEnableAutomaticRoleDuringSynchronization() {
		// default initialization of system and all necessary things
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setStartAutoRoleRec(true); // we want start recalculation after synchronization
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		this.getBean().deleteAllResourceData();
		
		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";
		
		String user1 = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(user1, testFirstName, testLastName);
		
		String user2 = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(user2, testFirstName, testLastName);
		
		String user3 = "test-3-" + System.currentTimeMillis();
		this.getBean().setTestData(user3, testFirstName, testLastName);
		
		IdmRoleDto role1 = helper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = helper.createAutomaticRole(role1.getId());
		helper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, user1);
		
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3,
				OperationResultType.WARNING);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		IdmIdentityDto identity1 = identityService.getByUsername(user1);
		IdmIdentityDto identity2 = identityService.getByUsername(user2);
		IdmIdentityDto identity3 = identityService.getByUsername(user3);
		
		// we must change username, after create contract is also save identity (change state)
		identity1.setUsername(user1 + System.currentTimeMillis());
		identity1 = identityService.save(identity1);
		
		helper.createIdentityContact(identity1);
		helper.createIdentityContact(identity2);
		helper.createIdentityContact(identity3);
		
		List<IdmIdentityRoleDto> identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		List<IdmIdentityRoleDto> identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		List<IdmIdentityRoleDto> identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());
		
		// enable test processor
		testIdentityProcessor.enable();
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(1, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());
		
		IdmIdentityRoleDto foundIdentityRole = identityRoles1.get(0);
		assertEquals(automaticRole.getId(), foundIdentityRole.getRoleTreeNode());
		
		// synchronization immediately recalculate is disabled
		int size = testIdentityProcessor.getRolesByUsername(user1).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user2).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user3).size();
		assertEquals(0, size);
	}
	
	@Test
	public void testDisableAutomaticRoleDuringSynchronization() {
		// default initialization of system and all necessary things
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setStartAutoRoleRec(false); // we want start recalculation after synchronization
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		this.getBean().deleteAllResourceData();
		
		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";
		
		String user1 = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(user1, testFirstName, testLastName);
		
		String user2 = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(user2, testFirstName, testLastName);
		
		String user3 = "test-3-" + System.currentTimeMillis();
		this.getBean().setTestData(user3, testFirstName, testLastName);
		
		IdmRoleDto role1 = helper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = helper.createAutomaticRole(role1.getId());
		helper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS, AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, user1);
		
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3,
				OperationResultType.WARNING);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		IdmIdentityDto identity1 = identityService.getByUsername(user1);
		IdmIdentityDto identity2 = identityService.getByUsername(user2);
		IdmIdentityDto identity3 = identityService.getByUsername(user3);
		
		// we must change username, after create contract is also save identity (change state)
		identity1.setUsername(user1 + System.currentTimeMillis());
		identity1 = identityService.save(identity1);
		
		helper.createIdentityContact(identity1);
		helper.createIdentityContact(identity2);
		helper.createIdentityContact(identity3);
		
		List<IdmIdentityRoleDto> identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		List<IdmIdentityRoleDto> identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		List<IdmIdentityRoleDto> identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());
		
		// enable test processor
		testIdentityProcessor.enable();
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());
		
		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());
		
		// synchronization immediately recalculate is disabled
		int size = testIdentityProcessor.getRolesByUsername(user1).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user2).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user3).size();
		assertEquals(0, size);
	}
	
	@Test
	public void testLinkAndUpdateIdentity() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		this.getBean().deleteAllResourceData();
		
		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name-";
		
		String user1 = "test-1-" + System.currentTimeMillis();
		IdmIdentityDto identity1 = helper.createIdentity(user1);
		this.getBean().setTestData(user1, testFirstName + 1, testLastName);
		
		String user2 = "test-2-" + System.currentTimeMillis();
		IdmIdentityDto identity2 = helper.createIdentity(user2);
		this.getBean().setTestData(user2, testFirstName + 2, testLastName);
		
		String user3 = "test-3-" + System.currentTimeMillis();
		IdmIdentityDto identity3 = helper.createIdentity(user3);
		this.getBean().setTestData(user3, testFirstName + 3, testLastName);
		
		assertNotEquals(testFirstName + 1, identity1.getFirstName());
		assertNotEquals(testFirstName + 2, identity2.getFirstName());
		assertNotEquals(testFirstName + 3, identity3.getFirstName());
		
		assertNotEquals(testLastName, identity1.getLastName());
		assertNotEquals(testLastName, identity2.getLastName());
		assertNotEquals(testLastName, identity3.getLastName());
		
		testIdentityProcessor.enable();
		synchornizationService.setSynchronizationConfigId(config.getId());
		synchornizationService.process();
		
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 3,
				OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		IdmIdentityDto updatedIdentity1 = identityService.getByUsername(user1);
		IdmIdentityDto updatedIdentity2 = identityService.getByUsername(user2);
		IdmIdentityDto updatedIdentity3 = identityService.getByUsername(user3);
		
		assertNotEquals(updatedIdentity1.getFirstName(), identity1.getFirstName());
		assertNotEquals(updatedIdentity2.getFirstName(), identity2.getFirstName());
		assertNotEquals(updatedIdentity3.getFirstName(), identity3.getFirstName());
		
		assertNotEquals(updatedIdentity1.getLastName(), identity1.getLastName());
		assertNotEquals(updatedIdentity2.getLastName(), identity2.getLastName());
		assertNotEquals(updatedIdentity3.getLastName(), identity3.getLastName());
		
		assertNotEquals(updatedIdentity1.getModified(), identity1.getModified());
		assertNotEquals(updatedIdentity2.getModified(), identity2.getModified());
		assertNotEquals(updatedIdentity3.getModified(), identity3.getModified());
		
		assertEquals(testFirstName + 1, updatedIdentity1.getFirstName());
		assertEquals(testFirstName + 2, updatedIdentity2.getFirstName());
		assertEquals(testFirstName + 3, updatedIdentity3.getFirstName());
		
		assertEquals(testLastName, updatedIdentity1.getLastName());
		assertEquals(testLastName, updatedIdentity2.getLastName());
		assertEquals(testLastName, updatedIdentity3.getLastName());
	}

	private SysSyncLogDto checkSyncLog(AbstractSysSyncConfigDto config, SynchronizationActionType actionType, int count,
			OperationResultType resultType) {
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

		Assert.assertEquals(resultType, actionLog.getOperationResult());
		SysSyncItemLogFilter itemLogFilter = new SysSyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLogDto> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(count, items.size());

		return log;
	}

	public SysSyncIdentityConfigDto doCreateSyncConfig(SysSystemDto system) {

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.IDENTITY);
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
		SysSyncIdentityConfigDto syncConfigCustom = new SysSyncIdentityConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(false);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(uidAttribute.getId());
		syncConfigCustom.setName(SYNC_CONFIG_NAME);
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigCustom = (SysSyncIdentityConfigDto) syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		return syncConfigCustom;
	}

	private SysSystemDto initData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.IDENTITY);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);
		createMapping(system, syncMapping);
		this.getBean().initIdentityData();
		return system;

	}
	
	/**
	 * Method set and persist data to test resource
	 */
	@Transactional
	public void setTestData(String name, String firstName, String lastName) {
		TestResource resourceUser = new TestResource();
		resourceUser.setName(name);
		resourceUser.setFirstname(firstName);
		resourceUser.setLastname(lastName);
		entityManager.persist(resourceUser);
	}

	@Transactional
	public void initIdentityData() {
		deleteAllResourceData();

		TestResource resourceUserOne = new TestResource();
		resourceUserOne.setName(IDENTITY_ONE);
		resourceUserOne.setFirstname(IDENTITY_ONE);
		resourceUserOne.setLastname(IDENTITY_ONE);
		resourceUserOne.setEavAttribute("1");
		entityManager.persist(resourceUserOne);

	}

	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (ATTRIBUTE_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName("username");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("firstName");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("lastName");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if (ATTRIBUTE_EMAIL.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("email");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			}
		});
	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestResource.TABLE_NAME);
		q.executeUpdate();
	}

	private IdentitySyncTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
