package eu.bcvsolutions.idm.acc.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestTreeResource;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;


/**
 * Role catalogue synchronization tests
 * 
 * @author Svanda
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Rollback(false)
@Service
public class DefaultRoleCatalogueProvisioningTest extends AbstractIntegrationTest {

	private static final String SYSTEM_NAME = "systemName";
	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String CHANGED = "changed";

	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmRoleCatalogueService treeNodeService;
	@Autowired
	private FormService formService;

	@Autowired
	DataSource dataSource;

	// Only for call method createTestSystem
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;
	private SysSystem system;

	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void provisioningA_CreateAccount_withOutMapping() {
		// Delete all resource data
		this.deleteAllResourceData();
		
		// Create root node in IDM tree
		IdmRoleCatalogueDto nodeRoot = new IdmRoleCatalogueDto();
		nodeRoot.setCode("P1");
		nodeRoot.setName(nodeRoot.getCode());
		nodeRoot.setParent(null);
		nodeRoot = treeNodeService.save(nodeRoot);

		// Create node in IDM tree
		IdmRoleCatalogueDto nodeOne = new IdmRoleCatalogueDto();
		nodeOne.setCode("P12");
		nodeOne.setName(nodeOne.getCode());
		nodeOne.setParent(nodeRoot.getId());
		nodeOne = treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);
	}
	
	@Test(expected = ProvisioningException.class) // Provisioning tree in incorrect order
	public void provisioningB_CreateAccounts_withException() {

		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setName("P1");

		IdmRoleCatalogueDto nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setName("P12");
		IdmRoleCatalogueDto nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);

		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);

		// Create mapping for provisioning
		this.initData();

		// Save IDM node (must invoke provisioning)
		// We didn't provisioning for root first ... expect throw exception
		treeNodeService.save(nodeOne);
	}
	
	@Test
	@Transactional
	public void provisioningC_CreateAccounts_correct() {
		
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setName("P1");
		
		IdmRoleCatalogueDto nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setName("P12");
		IdmRoleCatalogueDto nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);
		TestTreeResource root = entityManager.find(TestTreeResource.class, "P1");
		Assert.assertNull(root);
		
		// Save IDM node again (must invoke provisioning)
		// Root first
		treeNodeService.save(nodeRoot);
		// Node next
		treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		root = entityManager.find(TestTreeResource.class, "P1");
		Assert.assertNotNull(root);
		one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
	}
	
	
	@Test
	public void provisioningD_UpdateAccount() {
		
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setName("P1");
		
		IdmRoleCatalogueDto nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setName("P12");
		IdmRoleCatalogueDto nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
		Assert.assertEquals("P12", one.getCode());
		
		nodeOne.setCode(CHANGED);
		
		// Save IDM changed node (must invoke provisioning)
		treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
		Assert.assertEquals(CHANGED, one.getCode());
	}
	
	@Test(expected=ResultCodeException.class)
	public void provisioningE_DeleteAccount_IntegrityException() {
		
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setName("P1");
		
		IdmRoleCatalogueDto nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);
		
		// Delete IDM node (must invoke provisioning) .. We delete node with some children ... must throw integrity exception
		// Generally we counts with provisioning on every node ... include children (Recursively delete is not good idea!) 
		treeNodeService.delete(nodeRoot);
	}
	
	@Test
	public void provisioningF_DeleteAccount() {
		
		RoleCatalogueFilter filter = new RoleCatalogueFilter();
		filter.setName("P12");
		IdmRoleCatalogueDto nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Delete IDM node (must invoke provisioning) .. We delete child
		treeNodeService.delete(nodeOne);
		
		Assert.assertTrue(treeNodeService.find(filter, null).getContent().isEmpty());
	}
	
	

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM test_tree_resource");
		q.executeUpdate();
	}
	
	private void initData() {

		// create test system
		system = defaultSysAccountManagementServiceTest.createTestSystem("test_tree_resource");
		system.setName(SYSTEM_NAME);
		system = systemService.save(system);
		// key to EAV
		IdmFormDefinition savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());
		List<AbstractFormValue<SysSystem>> values = formService.getValues(system, savedFormDefinition);
		AbstractFormValue<SysSystem> changeLogColumn = values.stream().filter(value -> {return "keyColumn".equals(value.getFormAttribute().getCode());}).findFirst().get();
		formService.saveValues(system, changeLogColumn.getFormAttribute(), ImmutableList.of("ID"));
		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = systemService.generateSchema(system);
		
		// Create synchronization mapping
		SysSystemMapping syncSystemMapping = new SysSystemMapping();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.ROLE_CATALOGUE);
		syncSystemMapping.setOperationType(SystemOperationType.PROVISIONING);
		syncSystemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);


	}
	
	@Transactional
	public void changeOne(){
		TestTreeResource one = entityManager.find(TestTreeResource.class, "111");
		one.setCode(CHANGED);
		entityManager.persist(one);
	}
	
	@Transactional
	public void removeOne(){
		TestTreeResource one = entityManager.find(TestTreeResource.class, "111");
		entityManager.remove(one);
	}
	

	private void createMapping(SysSystem system, final SysSystemMapping entityHandlingResult) {
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (ATTRIBUTE_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				// For provisioning .. we need create UID
				attributeHandlingName.setTransformToResourceScript("if(uid){return uid;}\nreturn entity.getCode();");
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("CODE".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("code");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);
			
			} else if ("PARENT".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("parent");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("NAME".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("name");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			}
		});
	}

	private DefaultRoleCatalogueProvisioningTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
