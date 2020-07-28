package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Provides authorization evaluators to target read / write services.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManager implements AuthorizationManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuthorizationManager.class);
	//
	private IdmAuthorizationPolicyService service;
	private final ApplicationContext context;
	private final SecurityService securityService;
	private final ModuleService moduleService;
	//
	@Autowired private IdmCacheManager cacheManager;
	// evaluators cache
	private final Map<String, AuthorizationEvaluator<?>> evaluators = new HashMap<>();	
	
	public DefaultAuthorizationManager(
			ApplicationContext context,
			IdmAuthorizationPolicyService service,
			SecurityService securityService,
			ModuleService moduleService) {
		Assert.notNull(service, "Service is required.");
		Assert.notNull(context, "Context is required.");
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(moduleService, "Service is required.");
		//
		this.service = service;
		this.context = context;
		this.securityService = securityService;
		this.moduleService = moduleService;
	}
	
	@Override
	public <E extends Identifiable> Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, BasePermission... permission) {
		Assert.notNull(permission, "Permission is required");
		//
		// check super admin
		if (securityService.isAdmin()) {
			LOG.debug("Logged as admin [{}], authorization granted", securityService.getCurrentUsername());
			return builder.conjunction();
		}
		//
		final List<Predicate> predicates = Lists.newArrayList(); // no data by default
		//
		getEnabledDistinctPolicies(securityService.getCurrentId(), root.getJavaType()).forEach(policy -> {
			AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
			if (evaluator != null && evaluator.supports(root.getJavaType())) {
				Predicate predicate = evaluator.getPredicate(root, query, builder, policy, PermissionUtils.trimNull(permission));
				if (predicate != null) {
					predicates.add(predicate);
				}
			}
		});
		if (predicates.isEmpty()) {
			return builder.disjunction(); // no data by default
		}
		return builder.or(predicates.toArray(new Predicate[predicates.size()]));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends Identifiable> Set<String> getPermissions(E entity) {
		Assert.notNull(entity, "Entity is required.");
		final Set<String> permissions = new HashSet<>();
		//
		UUID loggedIdentityId = securityService.getCurrentId();
		if (loggedIdentityId == null) {
			// TODO: support setting policies to not logged user - e.g. public endpoints.
			return permissions;
		}
		// try to get cached permissions
		Serializable entityId = entity.getId();
		Map<Serializable, Set<String>> cachedPermissions = null;
		if (entityId != null) { // TODO: support cache for newly created entities without id
			ValueWrapper value = cacheManager.getValue(PERMISSION_CACHE_NAME, loggedIdentityId);
			if (value != null) {
				// cache value is never null
				cachedPermissions = new HashMap<>((Map) value.get());
				//
				if (cachedPermissions.containsKey(entityId)) {
					return cachedPermissions.get(entityId);
				}
			}		
		}
		// load policies and get permissions
		getEnabledDistinctPolicies(loggedIdentityId, entity.getClass()).forEach(policy -> {				
			permissions.addAll(getPermissions(entity, policy));
		});
		// cache permissions 
		if (entityId != null) {
			if (cachedPermissions == null) {
				cachedPermissions = new HashMap<>();
			}
			cachedPermissions.put(entityId, permissions);
			cacheManager.cacheValue(PERMISSION_CACHE_NAME, loggedIdentityId, cachedPermissions);
		}
		//
		return permissions;
	}
	
	@Override
	public <E extends Identifiable> Set<String> getPermissions(E entity, AuthorizationPolicy policy) {
		Assert.notNull(policy, "Policy is required.");
		//
		final Set<String> permissions = new HashSet<>();
		AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
		if (evaluator == null) {
			LOG.warn("Authorization evaluator for given policy [{}] not exists", policy.getId());
			return permissions;
		}
		Class<?> authorizableClass = resolveAuthorizableClass(entity, policy);
		if (authorizableClass != null && !evaluator.supports(authorizableClass)) {
			LOG.debug("Authorization evaluator [{}] not supports given authorizable type [{}]", 
					evaluator.getClass().getCanonicalName(), 
					authorizableClass.getClass().getCanonicalName());
			return permissions;
		}
		// evaluate permissions
		permissions.addAll(evaluator.getPermissions(entity, policy));
		//
		return permissions;
	}
	
	@Override
	public <E extends Identifiable> Set<String> getAuthorities(UUID identityId, Class<E> authorizableType) {
		Assert.notNull(authorizableType, "Authorizable type is required.");
		//
		final Set<String> authorities = new HashSet<>();
		getEnabledDistinctPolicies(identityId, authorizableType).forEach(policy -> {	
			authorities.addAll(getAuthorities(identityId, policy));
		});
		return authorities;
	}
	
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		Assert.notNull(policy, "Policy is required.");
		//
		final Set<String> authorities = new HashSet<>();
		AuthorizationEvaluator<?> evaluator = getEvaluator(policy);
		if (evaluator == null) {
			LOG.warn("Authorization evaluator for given policy [{}] not exists", policy.getId());
			return authorities;
		}
		//
		Class<?> authorizableClass = resolveAuthorizableClass(null, policy);
		if (authorizableClass != null && !evaluator.supports(authorizableClass)) {
			LOG.debug("Authorization evaluator [{}] not supports given authorizable type [{}]", 
					evaluator.getClass().getCanonicalName(), 
					authorizableClass.getClass().getCanonicalName());
			return authorities;
		}
		// evaluate authorities
		authorities.addAll(evaluator.getAuthorities(identityId, policy));
		//
		return authorities;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public <E extends Identifiable> boolean evaluate(E entity, BasePermission... permission) {
		Assert.notNull(entity, "Entity is required.");
		Assert.notNull(permission, "Permission is required");
		//
		// check super admin
		if (securityService.isAdmin()) {
			LOG.debug("Logged as admin [{}], authorization granted", securityService.getCurrentUsername());
			//
			return true;
		}
		//
		List<IdmAuthorizationPolicyDto> enabledPolicies = service.getEnabledPolicies(securityService.getCurrentId(), entity.getClass());
		LOG.debug("Found [{}] enabled authorization policies for authorizable type [{}]", enabledPolicies.size(), entity.getClass());
		for (IdmAuthorizationPolicyDto policy : enabledPolicies) {
			if (!supportsEntityType(policy, entity.getClass())) {
				// TODO: compatibility issues - agendas without authorization support
				continue;
			}
			AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
			// TODO: rewrite to use cached getPermission method instead in version 10.5.x
			if (evaluator != null && evaluator.supports(entity.getClass()) && evaluator.evaluate(entity, policy, permission)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<AuthorizationEvaluatorDto> getSupportedEvaluators() {
		// TODO: sort
		return context
			.getBeansOfType(AuthorizationEvaluator.class)
			.values()
			.stream()
			.map(evaluator -> {
				AuthorizationEvaluatorDto evaluatorDto = new AuthorizationEvaluatorDto();
				evaluatorDto.setId(evaluator.getId());
				evaluatorDto.setName(evaluator.getName());
				evaluatorDto.setEntityType(evaluator.getEntityClass().getCanonicalName());
				evaluatorDto.setEvaluatorType(AutowireHelper.getTargetType(evaluator));
				evaluatorDto.setModule(evaluator.getModule());
				evaluatorDto.setSupportsPermissions(evaluator.supportsPermissions());
				evaluatorDto.setDescription(evaluator.getDescription());
				evaluatorDto.setFormDefinition(evaluator.getFormDefinition());
				//
				return evaluatorDto;
			})
			.collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Services authorization policies support can be enabled / disabled dynamically
	 */
	@Override
	public Set<AuthorizableType> getAuthorizableTypes() {
		Set<AuthorizableType> authorizableTypes = new HashSet<>();
		// types with authorization evaluators support
		context.getBeansOfType(AuthorizableService.class).values().forEach(service -> {
			if (service.getAuthorizableType() != null) {
				authorizableTypes.add(service.getAuthorizableType());
			}
		});
		// add default - doesn't supports authorization evaluators
		moduleService.getAvailablePermissions().forEach(groupPermission -> {
			boolean exists = authorizableTypes.stream().anyMatch(authorizableType -> {
				// equals by group permission name only - name is identifier, base permission can be added in custom module
				return authorizableType.getGroup().getName().equals(groupPermission.getName());
			});
			if (!exists) {
				authorizableTypes.add(new AuthorizableType(groupPermission, null));
			}
		});
		return authorizableTypes;
	}
	
	/**
	 * Returns evaluator for given evaluator type.
	 * Checks, if policy evaluator is still on classpath - otherwise policy is disabled.
	 * 
	 * @param entityType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <E extends Identifiable> AuthorizationEvaluator<E> getEvaluator(AuthorizationPolicy policy) {
		String evaluatorType = policy.getEvaluatorType();
		if (!evaluators.containsKey(evaluatorType)) {
			try {
				evaluators.put(evaluatorType, (AuthorizationEvaluator<?>) context.getBean(Class.forName(evaluatorType)));
			} catch (ClassNotFoundException | NoSuchBeanDefinitionException ex) {
				// disable removed evaluator classes
				LOG.warn("Evaluator type [{}] for policy [{}] not found. Policy is ignored but should be disabled.", evaluatorType, policy.getId());
				return null;
			}
		}
		AuthorizationEvaluator<E> evaluator = (AuthorizationEvaluator<E>) evaluators.get(evaluatorType);
		if (evaluator.isDisabled()) {
			LOG.info("Evaluator type [{}] for policy [{}] is disabled - policy will not be evaluated.", evaluatorType, policy.getId());
			return null;
		}
		//
		return evaluator;
	}
	
	/**
	 * Returns true, when given policy supports given entityType. 
	 * AuthorizableType could be empty - we want to use predicates, but we don't want to support authorization policies.
	 * 
	 * @param policy
	 * @param entityType
	 * @return
	 */
	private boolean supportsEntityType(IdmAuthorizationPolicyDto policy, Class<? extends Identifiable> entityType) {
		return (StringUtils.isEmpty(policy.getGroupPermission()) && StringUtils.isEmpty(policy.getAuthorizableType()))
				|| IdmGroupPermission.APP.getName().equals(policy.getGroupPermission())
				|| entityType.getCanonicalName().equals(policy.getAuthorizableType());
	}
	
	/**
	 * Authorizable class from entity or policy.
	 * 
	 * @param entity
	 * @param policy
	 * @return
	 */
	private Class<?> resolveAuthorizableClass(Identifiable entity, AuthorizationPolicy policy) {
		if (entity != null) {
			return entity.getClass();
		}
		if (StringUtils.isNotBlank(policy.getAuthorizableType())) {
			try {
				return Class.forName(policy.getAuthorizableType());
			} catch (ClassNotFoundException ex) {
				LOG.warn("Class not found for authorizable type [{}], retuning empty permissions", policy.getAuthorizableType(), ex);
			}
		}
		return null;
	}
	
	/**
	 * Cache decorator - get or load current identity authorization policies.
	 * Distinct policies are returned only
	 * 
	 * @param identityId
	 * @param entityType
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<IdmAuthorizationPolicyDto> getEnabledDistinctPolicies(UUID identityId, Class<? extends Identifiable> entityType) {
		if (identityId == null) {
			// TODO: support setting policies to not logged user - e.g. public endpoints.
			return Lists.newArrayList();
		}
		Assert.notNull(entityType, "Entity type is required.");
		//
		// try to get cached policies
		Map<Class<? extends Identifiable>, List<IdmAuthorizationPolicyDto>> cachedPolicies;
		ValueWrapper value = cacheManager.getValue(AUTHORIZATION_POLICY_CACHE_NAME, identityId);
		if (value != null) {
			// cache value is never null - create copy
			cachedPolicies = new HashMap<>((Map) value.get());
		} else {
			cachedPolicies = new HashMap<>();
		}
		if (cachedPolicies.containsKey(entityType)) {
			return cachedPolicies.get(entityType);
		}
		// distinct policies
		List<IdmAuthorizationPolicyDto> enabledDistinctPolicies = new ArrayList<>();
		// load policies
		service
			.getEnabledPolicies(identityId, entityType)
			.stream()
			.filter(p -> supportsEntityType(p, entityType)) // TODO: compatibility issues - agendas without authorization support
			.forEach(policy -> {
				boolean contains = false;
				for (IdmAuthorizationPolicyDto registeredPolicy: enabledDistinctPolicies) {
					if (isDuplicate(policy, registeredPolicy)) {
						// policy with the same configuration is already registered
						contains = true;
						break;
					}
				}
				// register policy
				if (!contains) {
					enabledDistinctPolicies.add(policy);
				}
			});
		// cache policies 
		cachedPolicies.put(entityType, enabledDistinctPolicies);
		cacheManager.cacheValue(AUTHORIZATION_POLICY_CACHE_NAME, identityId, cachedPolicies);
		//
		return enabledDistinctPolicies;
	}
	
	/**
	 * Resolve duplicate policies => policies with the same configuration can be ignored.
	 *  
	 * @param one first policy
	 * @param two second policy
	 * @return
	 * @since 10.4.1
	 */
	protected boolean isDuplicate(IdmAuthorizationPolicyDto one, IdmAuthorizationPolicyDto two) {
		Assert.notNull(one, "Policy (one) is required.");
		Assert.notNull(two, "Policy (two) is required.");
		//
		// the same policy is assigned twice
		if (Objects.equal(one.getId(), two.getId())) {
			return true;
		}
		// policies are different, but configured the same way
		return Objects.equal(one.getAuthorizableType(), two.getAuthorizableType())
				&& Objects.equal(one.getEvaluatorType(), two.getEvaluatorType())
				&& Objects.equal(one.getGroupPermission(), two.getGroupPermission())
				&& Objects.equal(one.getPermissions(), two.getPermissions()) // set => order doesn't matter
				&& Objects.equal(one.getEvaluatorProperties(), two.getEvaluatorProperties());
	}
}
