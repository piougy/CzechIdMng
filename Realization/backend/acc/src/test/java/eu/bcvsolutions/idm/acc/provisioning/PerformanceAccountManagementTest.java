package eu.bcvsolutions.idm.acc.provisioning;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.entity.TestRoleResource;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Performance of ACM test - by default are all test skipped!
 * 
 * @author Vít Švanda
 *
 */
@Service
public class PerformanceAccountManagementTest extends AbstractIntegrationTest {

	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String ATTRIBUTE_EMAIL = "email";
	private static final String IDENTITY_PERFORMANCE = "identityPerformanceOne";

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
	private EntityManager entityManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	
	@Ignore
	@Test
	public void testAcmPerformance10() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);
		
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);
		IdmIdentityDto identity = helper.createIdentity();
		
		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		roleAccountFilter.setSystemId(system.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		// None role assigned
		Assert.assertEquals(0, identityAccounts.size());
		
		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 10);
		
		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();
		
		Date startAcm = new Date();
		
		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
		});
		
		Date endAcm = new Date();
		
		System.out.println("testAcmPerformance10 - ACM duration: "+ (endAcm.getTime() - startAcm.getTime()));
		
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(10, identityAccounts.size());
	}
	
	@Ignore
	@Test
	public void testAcmPerformance20() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);
		
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);
		IdmIdentityDto identity = helper.createIdentity();
		
		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		roleAccountFilter.setSystemId(system.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		// None role assigned
		Assert.assertEquals(0, identityAccounts.size());
		
		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 20);
		
		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();
		
		Date startAcm = new Date();
		
		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
		});
		
		Date endAcm = new Date();
		
		System.out.println("testAcmPerformance20 - ACM duration: "+ (endAcm.getTime() - startAcm.getTime()));
		
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(20, identityAccounts.size());
	}
	
	@Ignore
	@Test
	public void testAcmPerformance50() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);
		
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);
		IdmIdentityDto identity = helper.createIdentity();
		
		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		roleAccountFilter.setSystemId(system.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		// None role assigned
		Assert.assertEquals(0, identityAccounts.size());
		
		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 50);
		
		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();
		
		Date startAcm = new Date();
		
		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
		});
		
		Date endAcm = new Date();
		
		System.out.println("testAcmPerformance50 - ACM duration: "+ (endAcm.getTime() - startAcm.getTime()));
		
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(50, identityAccounts.size());
	}
	
	// @Ignore
	@Test
	@Transactional
	public void testAcmPerformance100() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);
		
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);
		IdmIdentityDto identity = helper.createIdentity();
		
		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		roleAccountFilter.setSystemId(system.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		// None role assigned
		Assert.assertEquals(0, identityAccounts.size());
		
		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 100);
		
		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();
		
		Date startAcm = new Date();
		
		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
			// Call hard hibernate session flush and clear
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});
		
		Date endAcm = new Date();
		
		System.out.println("testAcmPerformance100 - ACM duration: "+ (endAcm.getTime() - startAcm.getTime()));
		
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(100, identityAccounts.size());
	}

	
	//@Ignore
	@Test
	public void testAcmPerformanceAdd100One() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);

		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_PERFORMANCE);
		boolean first = false;
		if (identity == null) {
			identity = helper.createIdentity(IDENTITY_PERFORMANCE);
			first = true;
		}

		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null)
				.getContent();

		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 100);

		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());

		Date startAcm = new Date();
		

		IdmRoleRequestDto request = helper.createRoleRequest(primeContract, roles.toArray(new IdmRoleDto[0]));
		helper.executeRequest(request, false, true);

		Date endAcm = new Date();

		System.out.println("testAcmPerformance100One - ACM duration: " + (endAcm.getTime() - startAcm.getTime()));

		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		if (first) {
			Assert.assertEquals(100, identityAccounts.size());
		} else {
			Assert.assertEquals(200, identityAccounts.size());
		}
	}
	
	@Test
	public void testAcmPerformanceAdd100Two() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);

		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_PERFORMANCE);
		boolean first = false;
		if (identity == null) {
			identity = helper.createIdentity(IDENTITY_PERFORMANCE);
			first = true;
		}

		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null)
				.getContent();

		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 100);

		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());

		Date startAcm = new Date();

		IdmRoleRequestDto request = helper.createRoleRequest(primeContract, roles.toArray(new IdmRoleDto[0]));
		helper.executeRequest(request, false, true);

		Date endAcm = new Date();

		System.out.println("testAcmPerformance100Two - ACM duration: " + (endAcm.getTime() - startAcm.getTime()));

		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		if (first) {
			Assert.assertEquals(100, identityAccounts.size());
		} else {
			Assert.assertEquals(200, identityAccounts.size());
		}
	}

	
	// @Ignore
	@Test
	@Transactional
	public void testAcmPerformance200() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);
		
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);
		IdmIdentityDto identity = helper.createIdentity();
		
		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		roleAccountFilter.setSystemId(system.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		// None role assigned
		Assert.assertEquals(0, identityAccounts.size());
		
		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 200);
		
		 IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());

		Date startAcm = new Date();

		IdmRoleRequestDto request = helper.createRoleRequest(primeContract, roles.toArray(new IdmRoleDto[0]));
		helper.executeRequest(request, false, true);
		
		Date endAcm = new Date();
		
		
		System.out.println("testAcmPerformance200 - ACM duration: "+ (endAcm.getTime() - startAcm.getTime()));
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(200, identityAccounts.size());
	}
	

	
	@Ignore
	@Test
	@Transactional
	public void testDeletePerformance100() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);

		IdmIdentityDto identity = helper.createIdentity();

		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null)
				.getContent();

		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 100);

		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();

		Date startAcm = new Date();

		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});

		Date endAcm = new Date();

		System.out.println("testDeletePerformance100 - ACM duration: " + (endAcm.getTime() - startAcm.getTime()));

		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(100, identityAccounts.size());
		
		// Delete
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(primeContract);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		
		Date startAcmDelete = new Date();
		identityRoles.forEach(identityRole -> {
			identityRoleService.delete(identityRole);
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});
		
		Date endAcmDelete = new Date();
		System.out.println("testDeletePerformance100 - Delete duration: " + (endAcmDelete.getTime() - startAcmDelete.getTime()));
	}
	
	@Ignore
	@Test
	@Transactional
	public void testDeletePerformance200() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);

		IdmIdentityDto identity = helper.createIdentity();

		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null)
				.getContent();

		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 200);

		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();

		Date startAcm = new Date();

		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});

		Date endAcm = new Date();

		System.out.println("testDeletePerformance200 - ACM duration: " + (endAcm.getTime() - startAcm.getTime()));

		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(200, identityAccounts.size());
		
		// Delete
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(primeContract);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		
		Date startAcmDelete = new Date();
		identityRoles.forEach(identityRole -> {
			identityRoleService.delete(identityRole);
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});
		
		Date endAcmDelete = new Date();
		System.out.println("testDeletePerformance200 - Delete duration: " + (endAcmDelete.getTime() - startAcmDelete.getTime()));
	}
	
	@Ignore
	@Test
	@Transactional
	public void testDeletePerformance200WithSkip() {
		SysSystemDto system = initIdentityData();
		Assert.assertNotNull(system);

		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		Assert.assertNotNull(mapping);
		mapping = systemMappingService.save(mapping);

		IdmIdentityDto identity = helper.createIdentity();

		AccIdentityAccountFilter roleAccountFilter = new AccIdentityAccountFilter();
		roleAccountFilter.setEntityId(identity.getId());
		roleAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(roleAccountFilter, null)
				.getContent();

		List<IdmRoleDto> roles = this.createRolesWithSystem(system, 200);

		UUID primeContract = identityContractService.getPrimeContract(identity.getId()).getId();

		Date startAcm = new Date();

		roles.forEach(role -> {
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(primeContract);
			identityRole.setRole(role.getId());
			identityRole = identityRoleService.save(identityRole);
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
		});

		Date endAcm = new Date();

		System.out.println("testDeletePerformance200WithSkip - ACM duration: " + (endAcm.getTime() - startAcm.getTime()));

		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(200, identityAccounts.size());

		// Delete
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityContractId(primeContract);
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();

		IdmRoleRequestDto request = helper.createRoleRequest(identity);
		identityRoles.forEach(identityRole -> {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(request.getId());
			conceptRoleRequest.setIdentityContract(primeContract);
			conceptRoleRequest.setIdentityRole(identityRole.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequestService.save(conceptRoleRequest);
		});
		
		Date startAcmDelete = new Date();
		
		helper.executeRequest(request, false, true);

		Date endAcmDelete = new Date();
		System.out.println(
				"testDeletePerformance200WithSkip - Delete duration: " + (endAcmDelete.getTime() - startAcmDelete.getTime()));
		
		identityAccounts = identityAccountService.find(roleAccountFilter, null).getContent();
		Assert.assertEquals(0, identityAccounts.size());
	}




	private List<IdmRoleDto> createRolesWithSystem(SysSystemDto system, int numberOfRoles) {
		List<IdmRoleDto> roles = Lists.newArrayList();
		
		for(int i = 0; i < numberOfRoles; i++) {
			IdmRoleDto role = helper.createRole();
			helper.createRoleSystem(role, system);
			roles.add(role);
		}
		return roles;
	}

	

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestRoleResource.TABLE_NAME);
		q.executeUpdate();
	}
	
	private SysSystemDto initIdentityData() {

		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.IDENTITY);
		syncSystemMapping.setOperationType(SystemOperationType.PROVISIONING);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);
		createIdentityMapping(system, syncMapping);
		return system;

	}
	
	private void createIdentityMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
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
	
	private Session getHibernateSession() {
		return (Session) this.entityManager.getDelegate();
	}
}
