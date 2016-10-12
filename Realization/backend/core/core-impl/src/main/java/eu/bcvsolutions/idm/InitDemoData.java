package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška 
 *
 */
@Component
@DependsOn("initApplicationData")
public class InitDemoData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitDemoData.class);
	private static final String PARAMETER_DEMO_DATA_CREATED = "idm.sec.core.demo.data";
	private static final int FIRST_ROOT = 0; 
	
	@Autowired
	private InitApplicationData initApplicationData;
	
	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private IdmRoleRepository roleRepository;

	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;

	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;

	@Autowired
	private IdmIdentityContractRepository identityContractRepository;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private IdmConfigurationService configurationService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// we need to be ensured admin and and admin role exists.
		initApplicationData.init();
		//
		// TODO: runAs
		SecurityContextHolder.getContext().setAuthentication(
				new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities()));
		try {
			IdmRole superAdminRole = this.roleRepository.findOneByName(InitApplicationData.ADMIN_ROLE);
			IdmIdentity identityAdmin = this.identityRepository.findOneByUsername(InitApplicationData.ADMIN_USERNAME);
			//
			List<IdmTreeNode> rootsList = treeNodeRepository.findRoots(null);
			IdmTreeNode rootOrganization = null;
			if (!rootsList.isEmpty()) {
				rootOrganization = rootsList.get(FIRST_ROOT);
			} else {
				IdmTreeNode organizationRoot = new IdmTreeNode();
				organizationRoot.setName("Organization ROOT");
				organizationRoot.setTreeType(treeTypeRepository.findOneByCode(InitApplicationData.DEFAULT_TREE_TYPE));
				this.treeNodeRepository.save(organizationRoot);
			}
			//
			if (!configurationService.getBooleanValue(PARAMETER_DEMO_DATA_CREATED, false)) {
				log.info("Creating demo data ...");				
				//
				IdmRole role1 = new IdmRole();
				role1.setName("userRole");
				role1 = this.roleRepository.save(role1);
				log.info(MessageFormat.format("Role created [id: {0}]", role1.getId()));
				//
				IdmRole role2 = new IdmRole();
				role2.setName("customRole");
				List<IdmRoleComposition> subRoles = new ArrayList<>();
				subRoles.add(new IdmRoleComposition(role2, superAdminRole));
				role2.setSubRoles(subRoles);
				role2 = this.roleRepository.save(role2);
				role2.setApproveAddWorkflow("approveRoleByUserTomiska");
				log.info(MessageFormat.format("Role created [id: {0}]", role2.getId()));
				//
				IdmRole roleManager = new IdmRole();
				roleManager.setName("manager");
				roleManager = this.roleRepository.save(roleManager);
				log.info(MessageFormat.format("Role created [id: {0}]", roleManager.getId()));
				//
				//
				IdmIdentity identity = new IdmIdentity();
				identity.setUsername("tomiska");
				identity.setPassword("heslo".getBytes());
				identity.setFirstName("Radek");
				identity.setLastName("Tomiška");
				identity.setEmail("radek.tomiska@bcvsolutions.eu");
				identity = this.identityRepository.save(identity);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity.getId()));
				//
				IdmIdentityRole identityRole1 = new IdmIdentityRole();
				identityRole1.setIdentity(identity);
				identityRole1.setRole(role1);
				identityRoleRepository.save(identityRole1);
				//
				IdmIdentityRole identityRole2 = new IdmIdentityRole();
				identityRole2.setIdentity(identity);
				identityRole2.setRole(role2);
				identityRoleRepository.save(identityRole2);
				//
				IdmIdentity identity2 = new IdmIdentity();
				identity2.setUsername("svanda");
				identity2.setFirstName("Vít");
				identity2.setPassword("heslo".getBytes());
				identity2.setLastName("Švanda");
				identity2.setEmail("vit.svanda@bcvsolutions.eu");
				identity2 = this.identityRepository.save(identity2);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity2.getId()));
				//
				IdmIdentity identity3 = new IdmIdentity();
				identity3.setUsername("kopr");
				identity3.setFirstName("Ondrej");
				identity3.setPassword("heslo".getBytes());
				identity3.setLastName("Kopr");
				identity3.setEmail("ondrej.kopr@bcvsolutions.eu");
				identity3 = this.identityRepository.save(identity3);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity3.getId()));
				//
				// get tree type for organization
				IdmTreeType treeType = treeTypeRepository.findOneByCode(InitApplicationData.DEFAULT_TREE_TYPE);
				//
				IdmTreeNode organization1 = new IdmTreeNode();
				organization1.setName("Organization One");
				organization1.setParent(rootOrganization);
				organization1.setTreeType(treeType);
				this.treeNodeRepository.save(organization1);
				//
				IdmTreeNode organization2 = new IdmTreeNode();
				organization2.setName("Organization Two");
				organization2.setCreator("ja");
				organization2.setParent(rootOrganization);
				organization2.setTreeType(treeType);
				this.treeNodeRepository.save(organization2);
				//
				IdmIdentityContract identityWorkingPosition = new IdmIdentityContract();
				identityWorkingPosition.setIdentity(identityAdmin);
				identityWorkingPosition.setGuarantee(identity2);
				identityWorkingPosition.setWorkingPosition(organization2);
				identityContractRepository.save(identityWorkingPosition);
				//
				log.info("Demo data was created.");
				//				
				configurationService.setBooleanValue(PARAMETER_DEMO_DATA_CREATED, true);
			}
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
