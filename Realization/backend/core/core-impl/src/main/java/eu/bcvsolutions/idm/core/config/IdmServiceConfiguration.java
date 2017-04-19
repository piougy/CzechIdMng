package eu.bcvsolutions.idm.core.config;

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
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfidentialStorageValueRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmConfidentialStorage;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultModuleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultSubordinatesCriteriaBuilder;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
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
	@Autowired private IdmRoleTreeNodeRepository roleTreeNodeRepository;
	@Autowired private IdmTreeNodeRepository treeNodeRepository;
	@Autowired private IdmAuthorizationPolicyRepository authorizationPolicyRepository;
	@Autowired private IdmConfidentialStorageValueRepository confidentialStorageValueRepository;
	//
	// Own auto registered beans
	@Autowired private PluginRegistry<ModuleDescriptor, String> moduleDescriptorRegistry;
	
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
	public ConfigurationService configurationService() {
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
	 * @param context
	 * @param publisher
	 * @param enabledEvaluator
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
	 * @param service
	 * @param evaluators
	 * @return
	 */
	@Bean
	public AuthorizationManager authorizationManager(IdmRoleService roleService) {
		return new DefaultAuthorizationManager(context, authorizationPolicyService(roleService), securityService(), moduleService());
	}
	
	
	/**
	 * Long running task manager
	 * 
	 * @param service
	 * @param executor
	 * @param configurationService
	 * @param securityService
	 * @return
	 */
	@Bean
	public LongRunningTaskManager longRunningTaskManager(IdmLongRunningTaskService service) {
		return new DefaultLongRunningTaskManager(service, executor, configurationService(), securityService());
	}
	
	/**
	 * Service for assigning authorization evaluators to roles.
	 * 
	 * @param repository
	 * @return
	 */
	@Bean
	public IdmAuthorizationPolicyService authorizationPolicyService(IdmRoleService roleService) {
		return new DefaultIdmAuthorizationPolicyService(authorizationPolicyRepository, roleService, moduleService());
	}
	
	/**
	 * Subordinates criteria builder.
	 * 
	 * Override in custom module for changing subordinates evaluation.
	 * 
	 * @return
	 */
	@Bean
	public DefaultSubordinatesCriteriaBuilder subordinatesCriteriaBuilder() {
		return new DefaultSubordinatesCriteriaBuilder(identityRepository);
	}
	
	/**
	 * Automatic role service
	 * 
	 * @param repository
	 * @return
	 */
	@Bean
	public IdmRoleTreeNodeService roleTreeNodeService(IdmRoleRequestService roleRequestService, IdmConceptRoleRequestService conceptRoleRequestService) {
		return new DefaultIdmRoleTreeNodeService(roleTreeNodeRepository, treeNodeRepository, entityEventManager(), roleRequestService, conceptRoleRequestService);
	}
}
