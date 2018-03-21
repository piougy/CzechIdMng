package eu.bcvsolutions.idm.acc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.security.evaluator.IdentityAccountByAccountEvaluator;
import eu.bcvsolutions.idm.acc.security.evaluator.ReadAccountByIdentityEvaluator;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Initialization ACC module application data.
 * This component depends on {@link InitApplicationData}
 * 
 * For check demo data is used same property as in core
 * {@link InitDemoData#PARAMETER_DEMO_DATA_CREATED} and {@link InitDemoData#PARAMETER_DEMO_DATA_ENABLED}
 * 
 * TODO: do some refactor initialization app data on acc and core
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component(InitAccApplicationData.NAME)
@DependsOn(InitApplicationData.NAME)
public class InitAccApplicationData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitAccApplicationData.class);
	public static final String NAME = "initAccApplicationData";
	//
	@Autowired private SecurityService securityService;	
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SynchronizationService synchronizationService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (configurationService.getBooleanValue(InitDemoData.PARAMETER_DEMO_DATA_ENABLED, false)) {
			init();
		}
	}
	
	/**
	 * Initialize data for acc module
	 */
	protected void init() {
		securityService.setSystemAuthentication();
		//
		try {
			if (!configurationService.getBooleanValue(InitDemoData.PARAMETER_DEMO_DATA_CREATED, false)) {
				LOG.info("Creating demo data for [{}] module...", AccModuleDescriptor.MODULE_ID);
				// check if exist user role
				IdmRoleDto defaultRole = createDefaultRole();
				//
				// create default evaluators for acc module
				//
				LOG.info("Crea authorization [{}] for default user role.", IdentityAccountByAccountEvaluator.class.getSimpleName());
				IdmAuthorizationPolicyDto identityAccountByAccount = new IdmAuthorizationPolicyDto();
				identityAccountByAccount.setAuthorizableType(AccIdentityAccount.class.getCanonicalName());
				identityAccountByAccount.setEvaluator(IdentityAccountByAccountEvaluator.class);
				identityAccountByAccount.setGroupPermission(AccGroupPermission.IDENTITYACCOUNT.getName());
				identityAccountByAccount.setRole(defaultRole.getId());
				identityAccountByAccount = authorizationPolicyService.save(identityAccountByAccount);
				//
				LOG.info("Create authorization [{}] for default user role.", ReadAccountByIdentityEvaluator.class.getSimpleName());
				IdmAuthorizationPolicyDto accountByIdentity = new IdmAuthorizationPolicyDto();
				accountByIdentity.setAuthorizableType(AccAccount.class.getCanonicalName());
				accountByIdentity.setEvaluator(ReadAccountByIdentityEvaluator.class);
				accountByIdentity.setGroupPermission(AccGroupPermission.ACCOUNT.getName());
				accountByIdentity.setRole(defaultRole.getId());
				accountByIdentity = authorizationPolicyService.save(accountByIdentity);
				//
				// 
			}
			//
			// Cancels all previously ran tasks
			synchronizationService.init();
		} catch(Exception ex) {
			LOG.warn("Init data for ACC module, was not created!", ex);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
	
	/**
	 * Method check  if exists role with name/code defined in {@link InitDemoData#DEFAULT_ROLE_NAME}
	 * @return
	 */
	private IdmRoleDto createDefaultRole() {
		IdmRoleDto defaultRole = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
		if (defaultRole != null) {
			return defaultRole;
		}
		//
		defaultRole = new IdmRoleDto();
		defaultRole.setName(InitDemoData.DEFAULT_ROLE_NAME);
		return roleService.save(defaultRole);
	}
}
