package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmAuthorizationPolicyService.class);
	private final IdmAuthorizationPolicyRepository repository;
	private final IdmRoleService roleService;
	
	public DefaultIdmAuthorizationPolicyService(
			IdmAuthorizationPolicyRepository repository, 
			IdmRoleService roleService) {
		super(repository);
		//
		Assert.notNull(roleService);
		//
		this.repository = repository;
		this.roleService = roleService;
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
		List<IdmAuthorizationPolicyDto> results = toDtos(repository.getPolicies(username, entityType.getCanonicalName(), false, new LocalDate()), false);
		results.addAll(getDefaultPolicies());
		return results;
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
	
	@Override
	@Transactional(readOnly = true)
	public Set<String> getDefaultAuthorities() {
		IdmRole defaultRole = roleService.getDefaultRole();
		if (defaultRole == null) {
			LOG.debug("Default role not found, no default authorities will be added. Change configuration [{}].", IdmRoleService.PROPERTY_DEFAULT_ROLE);
			return Collections.<String>emptySet();
		}
		if (defaultRole.isDisabled()) {
			LOG.debug("Default role [{}] is disabled, no default authorities will be added.", defaultRole.getName());
			return Collections.<String>emptySet();
		}
		//
		Set<String> defaultAuthorities = defaultRole.getAuthorities()
				.stream()
				.map(authority -> {
					return authority.getAuthority();
				})
				.collect(Collectors.toSet());
		//
		LOG.debug("Found [{}] default authorities", defaultAuthorities.size());
		return defaultAuthorities;
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmAuthorizationPolicyDto> getDefaultPolicies() {
		IdmRole defaultRole = roleService.getDefaultRole();
		if (defaultRole == null) {
			LOG.debug("Default role not found, no default authorization policies will be added.  Change configuration [{}].", IdmRoleService.PROPERTY_DEFAULT_ROLE);
			return Collections.<IdmAuthorizationPolicyDto>emptyList();
		}
		if (defaultRole.isDisabled()) {
			LOG.debug("Default role [{}] is disabled, no default authorization policies will be added.", defaultRole.getName());
			return Collections.<IdmAuthorizationPolicyDto>emptyList();
		}
		//
		AuthorizationPolicyFilter filter = new AuthorizationPolicyFilter();
		filter.setRoleId(defaultRole.getId());
		filter.setDisabled(Boolean.FALSE);
		List<IdmAuthorizationPolicy> defaultPolicies = repository.find(filter, null).getContent();
		//
		LOG.debug("Found [{}] default policies", defaultPolicies.size());
		return toDtos(defaultPolicies, true);
	}
}
