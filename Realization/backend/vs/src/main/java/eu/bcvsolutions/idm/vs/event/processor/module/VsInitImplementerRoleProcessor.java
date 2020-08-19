package eu.bcvsolutions.idm.vs.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitRoleCatalogueProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitUserRoleProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.config.domain.VsConfiguration;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.evaluator.VsRequestByImplementerEvaluator;

/**
 * Init implementer role for vs module.
 * 
 * @see InitUserRoleProcessor
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(VsInitImplementerRoleProcessor.PROCESSOR_NAME)
@Description("Init implementer role for vs module (by configuration 'idm.sec.vs.role.implementer')."
		+ "Role is created, when not exist. "
		+ "Role will not be created, when configuration property is empty (defined, but empty string is given). "
		+ "Role is created with 'SYSTEM' role type - type is checked, when role authorities are created (or updated)."
		+ "Role is placed into role catalogue 'CzechIdM Roles' item, if item is defined.")
public class VsInitImplementerRoleProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VsInitImplementerRoleProcessor.class);
	public static final String PROCESSOR_NAME = "vs-init-implementer-role-processor";
	//
	@Autowired private VsConfiguration vsConfiguration;
	@Autowired private IdmRoleService roleService;
	@Autowired private InitRoleCatalogueProcessor initRoleCatalogueProcessor;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) && isInitDataEnabled();
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		IdmRoleDto implementerRole = vsConfiguration.getImplementerRole();
		if (implementerRole == null) {
			String impementerRoleCode = vsConfiguration.getImplementerRoleCode();
			if (impementerRoleCode == null) {
				LOG.warn("Implementer role does not exist and is not configured (by property [{}]). User role will not be created.", VsConfiguration.PROPERTY_IMPLEMENTER_ROLE);
				//
				return null;
			}
			implementerRole = new IdmRoleDto();
			implementerRole.setCode(impementerRoleCode);
			implementerRole.setName("Virtual system implementer");
			implementerRole.setDescription(PRODUCT_PROVIDED_ROLE_DESCRIPTION);
			implementerRole.setRoleType(RoleType.SYSTEM);
			implementerRole = roleService.save(implementerRole);
			//
			// register role into default catalogue
			initRoleCatalogueProcessor.registerRole(implementerRole);
			//
			LOG.info("Implementer role [{}] created.", impementerRoleCode);
		}
		if (implementerRole == null) {
			LOG.debug("Implementer role is not configured. Authorities will not be updated.");
			//
			return null;
		} else if (implementerRole.getRoleType() != RoleType.SYSTEM) {
			LOG.debug("Implementer role has not system type. Authorities will not be updated.");
			//
			return null;
		}
		//
		// find already configured role policies
		List<IdmAuthorizationPolicyDto> configuredPolicies = findConfiguredPolicies(implementerRole);
		//
		// read and solve one's requests on virtual systems
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setPermissions(IdmBasePermission.ADMIN);
		policy.setRole(implementerRole.getId());
		policy.setGroupPermission(VirtualSystemGroupPermission.VSREQUEST.getName());
		policy.setAuthorizableType(VsRequest.class.getCanonicalName());
		policy.setEvaluator(VsRequestByImplementerEvaluator.class);
		savePolicy(configuredPolicies, policy); 
		//
		// system entity - autocomplete
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after acc user role is created
		return CoreEvent.DEFAULT_ORDER + 20;
	}
}
