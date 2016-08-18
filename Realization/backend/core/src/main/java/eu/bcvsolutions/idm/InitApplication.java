package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.domain.CustomBasePermission;
import eu.bcvsolutions.idm.core.model.domain.CustomGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Initialize application
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Component
public class InitApplication implements ApplicationListener<ContextRefreshedEvent> {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitApplication.class);
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	private IdmOrganizationRepository organizationRepository;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository identityWorkingPositionRepository;
	
	@Autowired
	private SecurityService securityService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO: runAs
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAvailableAuthorities()));
		//
		try {
			IdmRole existsSuperAdminRole = this.roleRepository.findOneByName("superAdminRole");
			if (existsSuperAdminRole == null) {
				log.info("Creating demo data ...");
				//
				final IdmRole superAdminRole = new IdmRole();
				superAdminRole.setName("superAdminRole");
				superAdminRole.setRoleType(IdmRoleType.SYSTEM);
				superAdminRole.setApproveAddWorkflow("approveRoleBySuperAdminRole");				
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
				this.roleRepository.save(superAdminRole);
				log.info(MessageFormat.format("Role created [id: {0}]", superAdminRole.getId()));
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
				IdmIdentity identityAdmin = new IdmIdentity();
				identityAdmin.setUsername("admin");
				identityAdmin.setFirstName("Hlavní");
				identityAdmin.setPassword("heslo".getBytes());
				identityAdmin.setLastName("Administrátor");
				identityAdmin = this.identityRepository.save(identityAdmin);
				log.info(MessageFormat.format("Identity created [id: {0}]", identityAdmin.getId()));
				
				IdmIdentityRole identityRole4 = new IdmIdentityRole();
				identityRole4.setIdentity(identityAdmin);
				identityRole4.setRole(roleManager);
				identityRoleRepository.save(identityRole4);	
				IdmIdentityRole identityRole3 = new IdmIdentityRole();
				identityRole3.setIdentity(identityAdmin);
				identityRole3.setRole(superAdminRole);
				identityRoleRepository.save(identityRole3);		
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
				identity3 = this.identityRepository.save(identity2);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity3.getId()));
				//
				// create parent
				IdmOrganization organizationRoot = new IdmOrganization();
				organizationRoot.setName("Organization ROOT");
				this.organizationRepository.save(organizationRoot);
				//
				IdmOrganization organization1 = new IdmOrganization();
				organization1.setName("Organization One");
				organization1.setParent(organizationRoot);
				this.organizationRepository.save(organization1);
				//
				IdmOrganization organization2 = new IdmOrganization();
				organization2.setName("Organization Two");
				organization2.setCreator("ja");
				organization2.setParent(organizationRoot);
				this.organizationRepository.save(organization2);
				//
				IdmIdentityWorkingPosition identityWorkingPosition = new IdmIdentityWorkingPosition();
				identityWorkingPosition.setIdentity(identityAdmin);
				identityWorkingPosition.setPosition("vedoucí");
				identityWorkingPosition.setManager(identity2);
				identityWorkingPosition.setOrganization(organization2);
				identityWorkingPositionRepository.save(identityWorkingPosition);
				//
				log.info("Demo data was created.");
				//
				// TODO: split test and demo data - use flyway?
				//Users for JUnit testing
				IdmIdentity testUser1 = new IdmIdentity();
				testUser1.setUsername("testUser1");
				testUser1.setPassword("heslo".getBytes());
				testUser1.setFirstName("Test");
				testUser1.setLastName("First User");
				testUser1 = this.identityRepository.save(testUser1);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser1.getId()));
				this.identityRepository.save(testUser1);
				
				IdmIdentity testUser2 = new IdmIdentity();
				testUser2.setUsername("testUser2");
				testUser2.setPassword("heslo".getBytes());
				testUser2.setFirstName("Test");
				testUser2.setLastName("Second User");
				testUser2 = this.identityRepository.save(testUser2);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser2.getId()));
				this.identityRepository.save(testUser2);
			}
			//
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
