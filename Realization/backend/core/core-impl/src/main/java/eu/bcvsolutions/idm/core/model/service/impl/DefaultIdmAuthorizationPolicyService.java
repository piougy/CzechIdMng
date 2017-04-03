package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmAuthorizationPolicyService 
		extends AbstractReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicy, AuthorizationPolicyFilter> 
		implements IdmAuthorizationPolicyService {

	private IdmAuthorizationPolicyRepository repository;
	
	public DefaultIdmAuthorizationPolicyService(IdmAuthorizationPolicyRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmAuthorizationPolicyDto> getEnabledPolicies(UUID identityId, Class<? extends BaseEntity> entityType) {
		Assert.notNull(entityType);
		//
		return toDtos(repository.getPolicies(identityId, entityType.getCanonicalName(), false), false);
	}
}
