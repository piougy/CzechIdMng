package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;

/**
 * Initialize required application data:
 * * admin user admin/admin
 * * superAdminRole with system admin authority
 * 
 * @author Radek Tomiška 
 *
 */
@Component("initApplicationData")
public class InitApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitApplicationData.class);
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_PASSWORD = "admin";
	public static final String ADMIN_ROLE = "superAdminRole";
	public static final String DEFAULT_TREE_TYPE = "ORGANIZATIONS";

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
	private SecurityService securityService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// TODO: runAs
		SecurityContextHolder.getContext().setAuthentication(
				new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities()));
		// TODO: could be moved to flyway install dump
		try {
			//
			// create super admin role
			IdmRole existsSuperAdminRole = this.roleRepository.findOneByName(ADMIN_ROLE);
			if (existsSuperAdminRole == null && this.roleRepository.count() == 0) {
				//
				final IdmRole superAdminRole = new IdmRole();
				superAdminRole.setName(ADMIN_ROLE);
				superAdminRole.setRoleType(IdmRoleType.SYSTEM);
				superAdminRole.setApproveAddWorkflow("approveRoleBySuperAdminRole");
				superAdminRole.setApproveRemoveWorkflow("approveRemoveRoleBySuperAdminRole");
				superAdminRole.setRoleType(IdmRoleType.SYSTEM);
				List<IdmRoleAuthority> authorities = new ArrayList<>();
				securityService.getAvailableGroupPermissions().forEach(groupPermission -> {
					groupPermission.getPermissions().forEach(basePermission -> {
						IdmRoleAuthority privilege = new IdmRoleAuthority();
						privilege.setRole(superAdminRole);
						privilege.setTargetPermission(groupPermission);
						privilege.setActionPermission(basePermission);
						authorities.add(privilege);
					});
		
				});
				superAdminRole.setAuthorities(authorities);
				existsSuperAdminRole = this.roleRepository.save(superAdminRole);
				log.info(MessageFormat.format("Super admin Role created [id: {0}]", superAdminRole.getId()));
			}
			//
			// create super admin
			IdmIdentity existsSuperAdmin = this.identityRepository.findOneByUsername("admin");
			if (existsSuperAdmin == null && this.identityRepository.count() == 0) {
				//
				IdmIdentity identityAdmin = new IdmIdentity();
				identityAdmin.setUsername(ADMIN_USERNAME);
				identityAdmin.setPassword(ADMIN_PASSWORD.getBytes());
				identityAdmin.setLastName("Administrator");
				identityAdmin = this.identityRepository.save(identityAdmin);
				log.info(MessageFormat.format("Super admin identity created [id: {0}]", identityAdmin.getId()));
		
				
				IdmIdentityRole identityRole = new IdmIdentityRole();
				identityRole.setIdentity(identityAdmin);
				identityRole.setRole(existsSuperAdminRole);
				identityRoleRepository.save(identityRole);
			}
			// create Node type for organization
			IdmTreeType treeType = treeTypeRepository.findOneByCode(DEFAULT_TREE_TYPE);
			if (treeType == null) {
				treeType = new IdmTreeType();
				treeType.setCode(DEFAULT_TREE_TYPE);
				treeType.setName("Organization structure");
				this.treeTypeRepository.save(treeType);
			}
			//
			// create organization root
			if (treeNodeRepository.findRoots(treeType.getId()).isEmpty()) {
				IdmTreeNode organizationRoot = new IdmTreeNode();
				organizationRoot.setName("Root organization");
				organizationRoot.setTreeType(treeType);
				this.treeNodeRepository.save(organizationRoot);
			}
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
}
