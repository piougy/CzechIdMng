package eu.bcvsolutions.idm.core.config;

import java.util.List;
import java.util.concurrent.Executor;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.lookup.DtoLookup;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.config.domain.DefaultRoleConfiguration;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.FormValueService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultFormService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultIdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultIdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmContractGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultFilterManager;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleGuaranteeService;
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
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultLookupService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultModuleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmProcessedTaskItemRepository;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmScheduledTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultIdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultIdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultIdmScheduledTaskService;
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
 * Overridable core services initialization (configuration with lower order wins).
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
public class IdmServiceConfiguration {
	
	//
	// Spring environment
	@Autowired private ConfigurableEnvironment environment;
	@Autowired private ApplicationContext context;
	@Autowired private ApplicationEventPublisher publisher;
	@Autowired private Executor executor;
	@Autowired private EntityManager entityManager;
	//
	// Spring Data repositories through interfaces - they are constructed automatically
	@Autowired private IdmConfigurationRepository configurationRepository;
	@Autowired private IdmIdentityRepository identityRepository;
	@Autowired private IdmRoleRepository roleRepository;
	@Autowired private IdmRoleCatalogueRoleRepository roleCatalogueRoleRepository;
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
	@Autowired private IdmAuthorityChangeRepository authChangeRepository;
	@Autowired private IdmProcessedTaskItemRepository processedTaskRepository;
	@Autowired private IdmScheduledTaskRepository scheduledTaskRepository;
	@Autowired private IdmRoleGuaranteeRepository roleGuaranteeRepository;
	@Autowired private IdmPasswordRepository passwordRepository;
	//
	// Auto registered beans (plugins)
	@Autowired private PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry;
	@Autowired private List<? extends FormValueService<?, ?>> formValueServices;
	@Autowired private List<? extends FilterBuilder<?, ?>> filterBuilders;
	@Autowired private List<? extends EntityLookup<?>> entityLookups;
	@Autowired private List<? extends DtoLookup<?>> dtoLookups;
	
	/**
	 * Crypt service for confidential storage
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(CryptService.class)
	public CryptService cryptService() {
		return new DefaultCryptService();
	}
	
	/**
	 * Saving confidential values
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(ConfidentialStorage.class)
	public ConfidentialStorage confidentialStorage() {
		return new DefaultIdmConfidentialStorage(confidentialStorageValueRepository,  cryptService());
	}
	
	/**
	 * App configuration 
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmConfigurationService.class)
	public IdmConfigurationService configurationService() {
		return new DefaultConfigurationService(configurationRepository, confidentialStorage(), environment);
	}
	
	/**
	 * App modules
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(ModuleService.class)
	public ModuleService moduleService() {
		return new DefaultModuleService(moduleDescriptorRegistry, configurationService());
	}
	
	/**
	 * IdM authority hierarchy (group and app admin wildcards)
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(name = "roleHierarchy")
	public RoleHierarchy roleHierarchy() {
	    return new IdmAuthorityHierarchy(moduleService());
	}
	
	/**
	 * Enabled modules and enabled configuration property helper
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(EnabledEvaluator.class)
	public EnabledEvaluator enabledEvaluator() {
		return new DefaultEnabledEvaluator(moduleService(), configurationService());
	}
	
	/**
	 * Entity <=> Dto lookup service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(LookupService.class)
	public LookupService lookupService() {
		return new DefaultLookupService(context, entityManager, entityLookups, dtoLookups);
	}
	
	/**
	 * Event manager for entity event publishing.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(EntityEventManager.class)
	public EntityEventManager entityEventManager() {
		return new DefaultEntityEventManager(context, publisher, enabledEvaluator(), lookupService());
	}
	
	/**
	 * Authentication security service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(SecurityService.class)
	public SecurityService securityService() {
		return new DefaultSecurityService(roleHierarchy());
	}
	
	/**
	 * Filter manager for repositories
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(FilterManager.class)
	public FilterManager filterManager() {
		return new DefaultFilterManager(context, filterBuilders);
	}
	
	/**
	 * Authorization manager - authorization security service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(AuthorizationManager.class)
	public AuthorizationManager authorizationManager() {
		return new DefaultAuthorizationManager(context, authorizationPolicyService(), securityService(), moduleService());
	}
	
	/**
	 * EAV attributes
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmFormAttributeService.class)
	public IdmFormAttributeService formAttributeService() {
		return new DefaultIdmFormAttributeService(formAttributeRepository, formValueServices);
	}
	
	/**
	 * EAV definitions
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmFormDefinitionService.class)
	public IdmFormDefinitionService formDefinitionService() {
		return new DefaultIdmFormDefinitionService(formDefinitionRepository, formAttributeService());
	}
	
	/**
	 * EAV forms
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(FormService.class)
	public FormService formService() {
		return new DefaultFormService(formDefinitionService(), formAttributeService(), formValueServices, entityEventManager(), lookupService());
	}
	
	/**
	 * Configuration for role agenda
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(RoleConfiguration.class)
	public RoleConfiguration roleConfiguration() {
		return new DefaultRoleConfiguration(lookupService());
	}
	
	/**
	 * Role service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmRoleService.class)
	public IdmRoleService roleService() {
		return new DefaultIdmRoleService(
				roleRepository, 
				roleCatalogueRoleRepository,
				entityEventManager(), 
				formService(), 
				configurationService(), 
				filterManager(), 
				roleConfiguration());
	}
	
	/**
	 * Service for assigning authorization evaluators to roles.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmAuthorizationPolicyService.class)
	public IdmAuthorizationPolicyService authorizationPolicyService() {
		return new DefaultIdmAuthorizationPolicyService(authorizationPolicyRepository, roleService(),
				moduleService(), entityEventManager());
	}

	/**
	 * Persists long running tasks
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmLongRunningTaskService.class)
	public IdmLongRunningTaskService longRunningTaskService() {
		return new DefaultIdmLongRunningTaskService(longRunningTaskRepository, processedTaskItemService());
	}
	
	/**
	 * Long running task manager
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(LongRunningTaskManager.class)
	public LongRunningTaskManager longRunningTaskManager() {
		return new DefaultLongRunningTaskManager(
				longRunningTaskService(), 
				executor, 
				entityEventManager(),
				configurationService(), 
				securityService());
	}
	
	/**
	 * Identity service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmIdentityService.class)
	public IdmIdentityService identityService() {
		return new DefaultIdmIdentityService(
				identityRepository, 
				formService(),
				roleService(), 
				entityEventManager(),
				authChangeRepository,
				roleConfiguration());
	}
	
	/**
	 * Saves and crypts identity's password
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmPasswordService.class)
	public IdmPasswordService passwordService() {
		return new DefaultIdmPasswordService(passwordRepository);
	}
	
	/**
	 * Automatic role service
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmRoleTreeNodeService.class)
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
	@ConditionalOnMissingBean(IdmContractGuaranteeService.class)
	public IdmContractGuaranteeService contractGuaranteeService() {
		return new DefaultIdmContractGuaranteeService(contractGuaranteeRepository);
	}
	
	/**
	 * Assigned identity's contract
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmIdentityContractService.class)
	public IdmIdentityContractService identityContractService() {
		return new DefaultIdmIdentityContractService(identityContractRepository, formService(), entityEventManager(), treeTypeRepository, treeNodeRepository);
	}
	
	/**
	 * Assigned identity's roles 
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmIdentityRoleService.class)
	public IdmIdentityRoleService identityRoleService() {
		return new DefaultIdmIdentityRoleService(identityRoleRepository, entityEventManager());
	}

	/**
	 * Processed long running task items service.
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmProcessedTaskItemService.class)
	public IdmProcessedTaskItemService processedTaskItemService() {
		return new DefaultIdmProcessedTaskItemService(processedTaskRepository);
	}
	
	/**
	 * Scheduled tasks service for stateful tasks support.
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmScheduledTaskService.class)
	public IdmScheduledTaskService scheduledTaskService() {
		return new DefaultIdmScheduledTaskService(scheduledTaskRepository, processedTaskItemService(),
				longRunningTaskService());
	}
	
	/**
	 * Role guarantees
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(IdmRoleGuaranteeService.class)
	public IdmRoleGuaranteeService roleGuaranteeService() {
		return new DefaultIdmRoleGuaranteeService(roleGuaranteeRepository);
	}
}
