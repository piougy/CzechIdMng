package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Init administrator identity.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitAdminIdentityProcessor.PROCESSOR_NAME)
@Description("Init administrator identity with 'admin' username. "
		+ "Admin identity is not created, when admin role is not created (not configured by property 'idm.sec.core.role.admin' or deleted). "
		+ "Admin identity is not created, when other identity with admin role (configured by property 'idm.sec.core.role.admin') exists. "
		+ "Admin identity is created with password with never ending expiration. "
		+ "Admin identity is created with profile with show system informaiton is enabled. "
		+ "Change admin password is recomended after identity is created.")
public class InitAdminIdentityProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitAdminIdentityProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-admin-identity-processor";
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_PASSWORD = "admin";
	//
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmProfileService profileService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		if (adminRole == null) {
			LOG.warn("Admin role is not configured. Admin identity cannot be created, skipping.");
			//
			return null;
		}
		//
		// Create admin, if no other valid identity with admin role exists.
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setRoles(Lists.newArrayList(adminRole.getId()));
		filter.setDisabled(Boolean.FALSE);
		long adminCount = identityService.count(filter);
		if (adminCount > 0) {
			LOG.debug("Super admin identities found [{}], were created before. Admin with username [{}] will not be created.", adminCount, ADMIN_USERNAME);
			//
			return null;
		}
		//
		// create admin identity
		IdmIdentityDto identityAdmin = new IdmIdentityDto();
		identityAdmin.setUsername(ADMIN_USERNAME);
		identityAdmin.setPassword(new GuardedString(ADMIN_PASSWORD));
		identityAdmin.setLastName("Administrator");
		identityAdmin = identityService.save(identityAdmin);
		//
		// set never expires to identity password
		IdmPasswordDto adminPassword = passwordService.findOneByIdentity(identityAdmin.getId());
		adminPassword.setPasswordNeverExpires(true);
		passwordService.save(adminPassword);
		//
		LOG.info("Admin identity created [{}]", ADMIN_USERNAME);
		//
		// set show system information to profile
		IdmProfileDto adminProfile = profileService.findOrCreateByIdentity(identityAdmin.getId());
		adminProfile.setSystemInformation(true);
		profileService.save(adminProfile);
		//
		// create prime contract (required for assigned role)
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(identityAdmin.getId());
		if (contract == null) {
			contract = identityContractService.prepareMainContract(identityAdmin.getId());
			contract = identityContractService.save(contract);
		}
		//
		// assign admin role
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(contract.getId());
		identityRole.setRole(adminRole.getId());
		identityRoleService.save(identityRole);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after admin role is created
		return CoreEvent.DEFAULT_ORDER + 100;
	}
}
