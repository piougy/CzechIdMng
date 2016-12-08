package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitApplicationData.class);
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_PASSWORD = "admin";
	public static final String ADMIN_ROLE = "superAdminRole";
	public static final String DEFAULT_TREE_TYPE = "ORGANIZATIONS";

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	
	@Autowired
	private IdmTreeTypeService treeTypeService;

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
			IdmRole existsSuperAdminRole = this.roleService.getByName(ADMIN_ROLE);
			if (existsSuperAdminRole == null && this.roleService.find(new PageRequest(0, 1)).getTotalElements() == 0) {
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
				existsSuperAdminRole = this.roleService.save(superAdminRole);
				LOG.info(MessageFormat.format("Super admin Role created [id: {0}]", superAdminRole.getId()));
			}
			//
			// create super admin
			IdmIdentity existsSuperAdmin = this.identityService.getByUsername("admin");
			if (existsSuperAdmin == null && this.identityService.find(new PageRequest(0, 1)).getTotalElements() == 0) {
				//
				IdmIdentity identityAdmin = new IdmIdentity();
				identityAdmin.setUsername(ADMIN_USERNAME);
				identityAdmin.setPassword(new GuardedString(ADMIN_PASSWORD));
				identityAdmin.setLastName("Administrator");
				identityAdmin = this.identityService.save(identityAdmin);
				LOG.info(MessageFormat.format("Super admin identity created [id: {0}]", identityAdmin.getId()));
		
				
				IdmIdentityRole identityRole = new IdmIdentityRole();
				identityRole.setIdentity(identityAdmin);
				identityRole.setRole(existsSuperAdminRole);
				identityRoleService.save(identityRole);
			}
			// create Node type for organization
			
			IdmTreeType treeType = treeTypeService.getByCode(DEFAULT_TREE_TYPE);
			if (treeType == null) {
				treeType = new IdmTreeType();
				treeType.setCode(DEFAULT_TREE_TYPE);
				treeType.setName("Organization structure");
				this.treeTypeService.save(treeType);
			}
			//
			// create organization root
			if (treeNodeService.findRoots(treeType.getId(), new PageRequest(0, 1)).getTotalElements() == 0) {
				IdmTreeNode organizationRoot = new IdmTreeNode();
				organizationRoot.setCode("root");
				organizationRoot.setName("Root organization");
				organizationRoot.setTreeType(treeType);
				this.treeNodeService.save(organizationRoot);
			}
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
}
