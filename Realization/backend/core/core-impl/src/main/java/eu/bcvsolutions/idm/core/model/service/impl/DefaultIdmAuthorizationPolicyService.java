package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmAuthorizationPolicyService 
		extends AbstractReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicy, AuthorizationPolicyFilter> 
		implements IdmAuthorizationPolicyService {

	private final IdmAuthorizationPolicyRepository repository;
	
	public DefaultIdmAuthorizationPolicyService(IdmAuthorizationPolicyRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(getEntityClass(), CoreGroupPermission.AUTHORIZATIONPOLICY);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmAuthorizationPolicyDto> getEnabledPolicies(String username, Class<? extends Identifiable> entityType) {
		Assert.notNull(entityType);
		//
		return toDtos(repository.getPolicies(username, entityType.getCanonicalName(), false, new LocalDate()), false);
	}
	
	@Override
	protected Predicate toPredicate(AuthorizationPolicyFilter filter, Root<IdmAuthorizationPolicy> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		// role id
		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get("role").get("id"), filter.getRoleId()));
		}
		return builder.and(predicates.toArray(new Predicate[predicates.size()]));
	}
}
