package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Initialize test data for application.
 * 
 * @author Radek Tomiška 
 * @since 10.5.0
 */
@Profile("test")
@Component(InitTestDataProcessor.PROCESSOR_NAME)
@Description("Initialize test data for application.")
public class InitTestDataProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitTestDataProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-test-data-processor";
	private static final String PARAMETER_TEST_DATA_CREATED = "idm.sec.core.test.data";
	//
	public static final String TEST_ADMIN_USERNAME = InitAdminIdentityProcessor.ADMIN_USERNAME;
	public static final String TEST_ADMIN_PASSWORD = InitAdminIdentityProcessor.ADMIN_PASSWORD;
	public static final String TEST_USER_1 = "testUser1";
	public static final String TEST_USER_2 = "testUser2";
	/**
	 * @deprecated @since 10.5.0 - use {@link RoleConfiguration#getAdminRoleCode()}
	 */
	@Deprecated
	public static final String TEST_ADMIN_ROLE = RoleConfiguration.DEFAULT_ADMIN_ROLE;
	public static final String TEST_USER_ROLE = "testUserRole";
	public static final String TEST_CUSTOM_ROLE = "testCustomRole";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmTreeNodeService treeNodeService;	
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		IdmTreeTypeDto treeType = treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE);
		IdmTreeNodeDto rootOrganization = treeNodeService.findRoots(treeType.getId(), PageRequest.of(0, 1)).getContent().get(0);
		//
		if (!configurationService.getBooleanValue(PARAMETER_TEST_DATA_CREATED, false)) {
			LOG.info("Creating test data ...");		
			//
			IdmRoleDto role1 = new IdmRoleDto();
			role1.setCode(TEST_USER_ROLE);
			role1 = this.roleService.save(role1);
			LOG.info("Test role created [id: {}]", role1.getId());
			//
			IdmRoleDto role2 = new IdmRoleDto();
			role2.setCode(TEST_CUSTOM_ROLE);
			role2 = this.roleService.save(role2);
			LOG.info("Test role created [id: {}]", role2.getId());
			//
			// Users for JUnit testing
			IdmIdentityDto testUser1 = new IdmIdentityDto();
			testUser1.setUsername(TEST_USER_1);
			testUser1.setPassword(new GuardedString("heslo"));
			testUser1.setFirstName("Test");
			testUser1.setLastName("First User");
			testUser1.setEmail("test1@bscsolutions.eu");
			testUser1 = this.identityService.save(testUser1);
			LOG.info("Identity created [id: {}]", testUser1.getId());				

			IdmIdentityDto testUser2 = new IdmIdentityDto();
			testUser2.setUsername(TEST_USER_2);
			testUser2.setPassword(new GuardedString("heslo"));
			testUser2.setFirstName("Test");
			testUser2.setLastName("Second User");
			testUser2.setEmail("test2@bscsolutions.eu");
			testUser2 = this.identityService.save(testUser2);
			LOG.info("Identity created [id: {}]", testUser2.getId());
		
			IdmTreeTypeDto type = this.treeTypeService.get(rootOrganization.getTreeType());
			
			IdmTreeNodeDto organization = new IdmTreeNodeDto();
			organization.setCode("test");
			organization.setName("Organization Test");
			organization.setCreator("ja");
			organization.setParent(rootOrganization.getId());
			organization.setTreeType(type.getId());
			organization = this.treeNodeService.save(organization);
			
			IdmIdentityContractDto identityWorkPosition2 = new IdmIdentityContractDto();
			identityWorkPosition2.setIdentity(testUser1.getId());
			identityWorkPosition2.setWorkPosition(organization.getId());
			identityWorkPosition2 = identityContractService.save(identityWorkPosition2);
			IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto();
			contractGuarantee.setIdentityContract(identityWorkPosition2.getId());
			contractGuarantee.setGuarantee(testUser2.getId());
			contractGuaranteeService.save(contractGuarantee);
			//
			LOG.info("Test data was created.");
			//
			configurationService.setBooleanValue(PARAMETER_TEST_DATA_CREATED, true);
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after demo data is created
		return CoreEvent.DEFAULT_ORDER + 4000;
	}
}
