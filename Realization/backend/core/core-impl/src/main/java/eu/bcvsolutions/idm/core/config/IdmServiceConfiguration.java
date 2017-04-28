package eu.bcvsolutions.idm.core.config;

import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultFormService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultIdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultIdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmContractGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultSubordinatesFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.SubordinatesFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmConfidentialStorage;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultModuleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultIdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultLongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultAuthorizationManager;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCryptService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultEnabledEvaluator;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.IdmAuthorityHierarchy;

/**
 * Overridable core services initialization
 * 
 * TODO: move all @Service annotated beans here
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class IdmServiceConfiguration {
	
	//
	// Spring environment
	@Autowired private ConfigurableEnvironment environment;
	@Autowired private ApplicationContext context;
	@Autowired private ApplicationEventPublisher publisher;
	@Autowired private Executor executor;
	//
	// Spring Data repositories through interfaces - they are constructed automatically
	@Autowired private IdmConfigurationRepository configurationRepository;
	@Autowired private IdmIdentityRepository identityRepository;
	@Autowired private IdmRoleRepository roleRepository;
	@Autowired private IdmRoleTreeNodeRepository roleTreeNodeRepository;
	@Autowired private IdmTreeTypeRepository treeTypeRepository;
	@Autowired private IdmTreeNodeRepository treeNodeRepository;
	@Autowired private IdmAuthorizationPolicyRepository authorizationPolicyRepository;
	@Autowired private IdmConfidentialStorageValueRepository confidentialStorageValueRepository;
	@Autowired private IdmFormDefinitionRepository formDefinitionRepository;
	@Autowired private IdmFormAttributeRepository formAttributeRepository;
	@Autowired private IdmLongRunningTaskRepository longRunningTaskRepository;
	@Autowired private IdmContractGuaranteeRepository contractGuaranteeRepository;
	@Autowired private IdmIdentityRoleRepository identityRoleRepository;
	@Autowired private IdmIdentityContractRepository identityContractRepository;
	//
	// Auto registered beans (plugins)
	@Autowired private PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry;
	@Autowired private List<? extends FormValueService<?, ?>> formValueServices;
	
	/**
	 * Crypt service for confidential storage
	 * 
	 * @return
	 */
	@Bean
	public CryptService cryptService() {
		return new DefaultCryptService();
	}
	
	/**
	 * Saving confidential values
	 * 
	 * @return
	 */
	@Bean
	public ConfidentialStorage confidentialStorage() {
		return new DefaultIdmConfidentialStorage(confidentialStorageValueRepository,  cryptService());
	}
	
	/**
	 * App configuration 
	 * 
	 * @return
	 */
	@Bean
	public IdmConfigurationService configurationService() {
		return new DefaultConfigurationService(configurationRepository, confidentialStorage(), environment);
	}
	
	/**
	 * App modules
	 * 
	 * @return
	 */
	@Bean
	public ModuleService moduleService() {
		return new DefaultModuleService(moduleDescriptorRegistry, configurationService());
	}
	
	/**
	 * IdM authority hierarchy (group and app admin wildcards)
	 * 
	 * @return
	 */
	@Bean
	public RoleHierarchy roleHierarchy() {
	    return new IdmAuthorityHierarchy(moduleService());
	}
	
	/**
	 * Enabled modules and enabled configuration property helper
	 * 
	 * @return
	 */
	@Bean
	public EnabledEvaluator enabledEvaluator() {
		return new DefaultEnabledEvaluator(moduleService(), configurationService());
	}
	
	/**
	 * Event manager for entity event publishing.
	 * 
	 * @return
	 */
	@Bean
	public EntityEventManager entityEventManager() {
		return new DefaultEntityEventManager(context, publisher, enabledEvaluator());
	}
	
	/**
	 * Authentication security service
	 * 
	 * @return
	 */
	@Bean
	public SecurityService securityService() {
		return new DefaultSecurityService(roleHierarchy());
	}
	
	/**
	 * Authorization manager - authorization security service
	 * 
	 * @return
	 */
	@Bean
	public AuthorizationManager authorizationManager() {
		return new DefaultAuthorizationManager(context, authorizationPolicyService(), securityService(), moduleService());
	}
	
	/**
	 * EAV attributes
	 * 
	 * @return
	 */
	@Bean
	public IdmFormAttributeService formAttributeService() {
		return new DefaultIdmFormAttributeService(formAttributeRepository, formValueServices);
	}
	
	/**
	 * EAV definitions
	 * 
	 * @return
	 */
	@Bean
	public IdmFormDefinitionService formDefinitionService() {
		return new DefaultIdmFormDefinitionService(formDefinitionRepository, formAttributeService());
	}
	
	/**
	 * EAV forms
	 * 
	 * @return
	 */
	@Bean
	public FormService formService() {
		return new DefaultFormService(formDefinitionService(), formAttributeService(), formValueServices, entityEventManager());
	}
	
	/**
	 * Role service
	 * 
	 * @return
	 */
	@Bean
	public IdmRoleService roleService() {
		return new DefaultIdmRoleService(roleRepository, entityEventManager(), formService(), configurationService());
	}
	
	/**
	 * Service for assigning authorization evaluators to roles.
	 * 
	 * @return
	 */
	@Bean
	public IdmAuthorizationPolicyService authorizationPolicyService() {
		return new DefaultIdmAuthorizationPolicyService(authorizationPolicyRepository, roleService(), moduleService());
	}
	
	/**
	 * Persists long running tasks
	 * 
	 * @return
	 */
	@Bean
	public IdmLongRunningTaskService longRunningTaskService() {
		return new DefaultIdmLongRunningTaskService(longRunningTaskRepository, configurationService());
	}
	
	
	/**
	 * Long running task manager
	 * 
	 * @return
	 */
	@Bean
	public LongRunningTaskManager longRunningTaskManager() {
		return new DefaultLongRunningTaskManager(longRunningTaskService(), executor, configurationService(), securityService());
	}
	
	/**
	 * Subordinates criteria builder.
	 * 
	 * Override in custom module for changing subordinates evaluation.
	 * 
	 * @return
	 */
	@Bean
	public SubordinatesFilter subordinatesFilter() {
		return new DefaultSubordinatesFilter(identityRepository);
	}
	
	/**
	 * Managers criteria builder.
	 * 
	 * Override in custom module for changing managers evaluation.
	 * 
	 * @return
	 */
	@Bean
	public ManagersFilter managersFilter() {
		return new DefaultManagersFilter(identityRepository);
	}
	
	/**
	 * Managers criteria builder (by contract id).
	 * 
	 * Override in custom module for changing managers evaluation.
	 * 
	 * @return
	 */
	@Bean
	public ManagersByContractFilter managersByContractFilter() {
		return new DefaultManagersByContractFilter(identityRepository);
	}
	
	/**
	 * Identity service
	 * 
	 * @return
	 */
	@Bean
	public IdmIdentityService identityService() {
		return new DefaultIdmIdentityService(identityRepository, formService(), roleRepository, entityEventManager(), subordinatesFilter(), managersFilter(), managersByContractFilter());
	}
	
	/**
	 * Automatic role service
	 * 
	 * @return
	 */
	@Bean
	public IdmRoleTreeNodeService roleTreeNodeService(
			IdmRoleRequestService roleRequestService, 
			IdmIdentityContractService identityContractService,
			IdmConceptRoleRequestService conceptRoleRequestService) {
		return new DefaultIdmRoleTreeNodeService(roleTreeNodeRepository, treeNodeRepository, entityEventManager(), roleRequestService, identityContractService, conceptRoleRequestService);
	}
	
	/**
	 * Identity's contract guarantee - manually defined  manager (if no tree structure is defined etc.)
	 * 
	 * @return
	 */
	@Bean
	public IdmContractGuaranteeService contractGuaranteeService() {
		return new DefaultIdmContractGuaranteeService(contractGuaranteeRepository);
	}
	
	/**
	 * Assigned identity's contract
	 * 
	 * @return
	 */
	@Bean
	public IdmIdentityContractService identityContractService() {
		return new DefaultIdmIdentityContractService(identityContractRepository, entityEventManager(), treeTypeRepository, treeNodeRepository);
	}
	
	/**
	 * Assigned identity's roles 
	 * 
	 * @return
	 */
	@Bean
	public IdmIdentityRoleService idmIdentityRoleService() {
		return new DefaultIdmIdentityRoleService(identityRoleRepository, entityEventManager());
	}	
}
