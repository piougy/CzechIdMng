package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška 
 *
 */
@Component
@Profile("test")
@DependsOn("initApplicationData")
public class InitTestData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitTestData.class);
	private static final String PARAMETER_TEST_DATA_CREATED = "idm.sec.core.test.data";
	
	public static final String HAL_CONTENT_TYPE = "application/hal+json";
	
	public static final String TEST_ADMIN_USERNAME = InitApplicationData.ADMIN_USERNAME;
	public static final String TEST_ADMIN_PASSWORD = InitApplicationData.ADMIN_PASSWORD;
	public static final String TEST_USER_1 = "testUser1";
	public static final String TEST_USER_2 = "testUser2";
	public static final String TEST_ADMIN_ROLE = InitApplicationData.ADMIN_ROLE;
	public static final String TEST_USER_ROLE = "testUserRole";
	public static final String TEST_CUSTOM_ROLE = "testCustomRole";
	
	@Autowired
	private InitDemoData initDemoData;	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmTreeNodeService treeNodeService;	
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired
	private SecurityService securityService;	
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// we are reusing demo data in tests as well
		initDemoData.init();
		//
		securityService.setSystemAuthentication();
		//
		try {
			IdmRoleDto superAdminRole = this.roleService.getByCode(InitApplicationData.ADMIN_ROLE);
			IdmTreeNodeDto rootOrganization = treeNodeService.findRoots((UUID) null, new PageRequest(0, 1)).getContent().get(0);
			//
			if (!configurationService.getBooleanValue(PARAMETER_TEST_DATA_CREATED, false)) {
				log.info("Creating test data ...");		
				//
				IdmRoleDto role1 = new IdmRoleDto();
				role1.setName(TEST_USER_ROLE);
				role1 = this.roleService.save(role1);
				log.info(MessageFormat.format("Test role created [id: {0}]", role1.getId()));
				//
				IdmRoleDto role2 = new IdmRoleDto();
				role2.setName(TEST_CUSTOM_ROLE);
				List<IdmRoleCompositionDto> subRoles = new ArrayList<>();
				subRoles.add(new IdmRoleCompositionDto(role2.getId(), superAdminRole.getId()));
				role2.setSubRoles(subRoles);
				role2 = this.roleService.save(role2);
				log.info(MessageFormat.format("Test role created [id: {0}]", role2.getId()));
				//
				// Users for JUnit testing
				IdmIdentityDto testUser1 = new IdmIdentityDto();
				testUser1.setUsername(TEST_USER_1);
				testUser1.setPassword(new GuardedString("heslo"));
				testUser1.setFirstName("Test");
				testUser1.setLastName("First User");
				testUser1.setEmail("test1@bscsolutions.eu");
				testUser1 = this.identityService.save(testUser1);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser1.getId()));				

				IdmIdentityDto testUser2 = new IdmIdentityDto();
				testUser2.setUsername(TEST_USER_2);
				testUser2.setPassword(new GuardedString("heslo"));
				testUser2.setFirstName("Test");
				testUser2.setLastName("Second User");
				testUser2.setEmail("test2@bscsolutions.eu");
				testUser2 = this.identityService.save(testUser2);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser2.getId()));
			
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
				log.info("Test data was created.");
				//
				configurationService.setBooleanValue(PARAMETER_TEST_DATA_CREATED, true);
			}
			//
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
