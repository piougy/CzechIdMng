package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.http.util.Asserts;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.TestContractResource;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Account tests
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccAccountServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void init() {
		loginAsAdmin("admin");
		this.getBean().deleteAllResourceData();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void getConnectorObjectTest() {
		String userOneName = "UserOne";
		String eavAttributeName = "EAV_ATTRIBUTE";
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		// Change resources (set state on exclude) .. must be call in transaction
		this.getBean().persistResource(createResource(userOneName, new LocalDateTime()));
		AccAccountDto account = new AccAccountDto();
		account.setEntityType(SystemEntityType.IDENTITY);
		account.setSystem(system.getId());
		account.setAccountType(AccountType.PERSONAL);
		account.setUid(userOneName);
		account = accountService.save(account);

		IcConnectorObject connectorObject = accountService.getConnectorObject(account);
		Assert.assertNotNull(connectorObject);
		Assert.assertEquals(userOneName, connectorObject.getUidValue());
		Assert.assertNotNull(connectorObject.getAttributeByName(eavAttributeName));
		Assert.assertEquals(userOneName, connectorObject.getAttributeByName(eavAttributeName).getValue());

	}

	@Test
	public void getConnectorObjectNotFullTest() {
		String userOneName = "UserOne";
		String eavAttributeName = "EAV_ATTRIBUTE";
		SysSystemDto system = initData();
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Find and delete EAV schema attribute. 
		SysSchemaAttributeDto eavAttribute = schemaAttributeService.find(schemaAttributeFilter, null).getContent()
				.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(eavAttributeName)).findFirst()
				.orElse(null);
		Assert.assertNotNull(eavAttribute);
		schemaAttributeService.delete(eavAttribute);
		Assert.assertNotNull(system);
		// Change resources (set state on exclude) .. must be call in transaction
		this.getBean().persistResource(createResource(userOneName, new LocalDateTime()));
		AccAccountDto account = new AccAccountDto();
		account.setEntityType(SystemEntityType.IDENTITY);
		account.setSystem(system.getId());
		account.setAccountType(AccountType.PERSONAL);
		account.setUid(userOneName);
		account = accountService.save(account);

		IcConnectorObject connectorObject = accountService.getConnectorObject(account);
		Assert.assertNotNull(connectorObject);
		Assert.assertEquals(userOneName, connectorObject.getUidValue());
		// EAV attribute must be null, because we deleted the schema definition
		Assert.assertNull(connectorObject.getAttributeByName(eavAttributeName));

	}

	@Transactional
	public void persistResource(TestResource resource) {
		entityManager.persist(resource);
	}

	private TestResource createResource(String code, LocalDateTime modified) {
		TestResource resource = new TestResource();
		resource.setName(code);
		resource.setEmail(code);
		resource.setDescrip(code);
		resource.setFirstname(code);
		resource.setLastname(code);
		resource.setModified(modified);
		resource.setStatus(code);
		resource.setEavAttribute(code);

		return resource;
	}

	private SysSystemDto initData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);

		// generate schema for system
		systemService.generateSchema(system);
		return system;

	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestResource.TABLE_NAME);
		q.executeUpdate();
	}

	private DefaultAccAccountServiceTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
