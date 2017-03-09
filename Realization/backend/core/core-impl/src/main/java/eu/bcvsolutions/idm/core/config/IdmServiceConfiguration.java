package eu.bcvsolutions.idm.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;

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

	@Bean
	public IdmRoleTreeNodeService roleTreeNodeService(AbstractEntityRepository<IdmRoleTreeNode, RoleTreeNodeFilter> repository) {
		return new DefaultIdmRoleTreeNodeService(repository);
	}
}
