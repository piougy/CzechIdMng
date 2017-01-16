package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

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
	private InitApplicationData initApplicationData;
	
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
	private SecurityService securityService;
	
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// we need to be ensured admin and and admin role exists.
		initApplicationData.init();
		//
		// TODO: runAs
		securityService.setAuthentication(
				new IdmJwtAuthentication(new IdentityDto("[SYSTEM]"), null, securityService.getAllAvailableAuthorities()));
		try {
			IdmRole superAdminRole = this.roleService.getByName(InitApplicationData.ADMIN_ROLE);
			IdmTreeNode rootOrganization = treeNodeService.findRoots(null, new PageRequest(0, 1)).getContent().get(0);
			//
			if (!configurationService.getBooleanValue(PARAMETER_TEST_DATA_CREATED, false)) {
				log.info("Creating test data ...");		
				//
				IdmRole role1 = new IdmRole();
				role1.setName(TEST_USER_ROLE);
				role1 = this.roleService.save(role1);
				log.info(MessageFormat.format("Test role created [id: {0}]", role1.getId()));
				//
				IdmRole role2 = new IdmRole();
				role2.setName(TEST_CUSTOM_ROLE);
				List<IdmRoleComposition> subRoles = new ArrayList<>();
				subRoles.add(new IdmRoleComposition(role2, superAdminRole));
				role2.setSubRoles(subRoles);
				role2 = this.roleService.save(role2);
				role2.setApproveAddWorkflow("approveRoleByUserTomiska");
				log.info(MessageFormat.format("Test role created [id: {0}]", role2.getId()));
				//
				// TODO: split test and demo data - use flyway?
				// Users for JUnit testing
				IdmIdentity testUser1 = new IdmIdentity();
				testUser1.setUsername(TEST_USER_1);
				testUser1.setPassword(new GuardedString("heslo"));
				testUser1.setFirstName("Test");
				testUser1.setLastName("First User");
				testUser1.setEmail("test1@bscsolutions.eu");
				testUser1 = this.identityService.save(testUser1);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser1.getId()));				

				IdmIdentity testUser2 = new IdmIdentity();
				testUser2.setUsername(TEST_USER_2);
				testUser2.setPassword(new GuardedString("heslo"));
				testUser2.setFirstName("Test");
				testUser2.setLastName("Second User");
				testUser2.setEmail("test2@bscsolutions.eu");
				testUser2 = this.identityService.save(testUser2);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser2.getId()));
			
				IdmTreeType type = new IdmTreeType();
				type.setCode("ROOT_TYPE");
				type.setName("ROOT_TYPE");
				this.treeTypeService.save(type);
				
				
				IdmTreeNode organization = new IdmTreeNode();
				organization.setCode("test");
				organization.setName("Organization Test");
				organization.setCreator("ja");
				organization.setParent(rootOrganization);
				organization.setTreeType(type);
				this.treeNodeService.save(organization);
				
				IdmIdentityContract identityWorkingPosition2 = new IdmIdentityContract();
				identityWorkingPosition2.setIdentity(testUser1);
				identityWorkingPosition2.setGuarantee(testUser2);
				identityWorkingPosition2.setWorkingPosition(organization);
				identityContractService.save(identityWorkingPosition2);
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
