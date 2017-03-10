package eu.bcvsolutions.idm.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Core services initialization
 * 
 * TODO: move all @Service annotated bean here
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class IdmServiceConfiguration {
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private EnabledEvaluator enabledEvaluator; // TODO: move to @Bean init here
	
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
		return new DefaultEntityEventManager(context, publisher, enabledEvaluator);
	}
	
	/**
	 * 
	 * @param repository
	 * @return
	 */
	@Bean
	public IdmRoleTreeNodeService roleTreeNodeService(AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository) {
		return new DefaultIdmRoleTreeNodeService(repository, entityEventManager());
	}
}
