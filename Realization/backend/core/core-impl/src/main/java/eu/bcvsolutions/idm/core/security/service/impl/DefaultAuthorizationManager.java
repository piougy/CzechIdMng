package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Provides authorization evaluators to target read / write services.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManager implements AuthorizationManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuthorizationManager.class);
	private final IdmAuthorizationPolicyService service;
	private final ApplicationContext context;
	private final SecurityService securityService;
	private final ModuleService moduleService;
	// cache
	private final Map<String, AuthorizationEvaluator<?>> evaluators = new HashMap<>();
	private Set<AuthorizableType> authorizableTypes = null;
	
	public DefaultAuthorizationManager(
			ApplicationContext context,
			IdmAuthorizationPolicyService service,
			SecurityService securityService,
			ModuleService moduleService) {
		Assert.notNull(service);
		Assert.notNull(context);
		Assert.notNull(securityService);
		Assert.notNull(moduleService);
		//
		this.service = service;
		this.context = context;
		this.securityService = securityService;
		this.moduleService = moduleService;
	}
	
	@Override
	public <E extends Identifiable> Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, BasePermission... permission) {
		final List<Predicate> predicates = Lists.newArrayList(builder.disjunction()); // disjunction - no data by default
		//S
		if (securityService.isAuthenticated()) { // TODO: public data?
			service.getEnabledPolicies(securityService.getUsername(), root.getJavaType()).forEach(policy -> {
				AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
				if (evaluator != null && evaluator.supports(root.getJavaType())) {
					Predicate predicate = evaluator.getPredicate(root, query, builder, policy, permission);
					if (predicate != null) {
						predicates.add(predicate);
					}
				}
			});	
		}
		return builder.or(predicates.toArray(new Predicate[predicates.size()]));
	}

	@Override
	public <E extends Identifiable> Set<String> getPermissions(E entity) {
		Assert.notNull(entity);
		//
		final Set<String> permissions = new HashSet<>();
		if (securityService.isAuthenticated()) { // TODO: public data?
			service.getEnabledPolicies(securityService.getUsername(), entity.getClass()).forEach(policy -> {
				AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
				if (evaluator != null && evaluator.supports(entity.getClass())) {
					permissions.addAll(evaluator.getPermissions(entity, policy));
				}
			});
		}
		return permissions;
	}
	
	@Override
	public <E extends Identifiable> boolean evaluate(E entity, BasePermission... permission) {
		Assert.notNull(entity);
		//
		if (securityService.isAuthenticated()) { // TODO: public data?
			for (IdmAuthorizationPolicyDto policy : service.getEnabledPolicies(securityService.getUsername(), entity.getClass())) {
				AuthorizationEvaluator<E> evaluator = getEvaluator(policy);
				if (evaluator != null && evaluator.supports(entity.getClass()) && evaluator.evaluate(entity, policy, permission)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns evaluator for given evaluator type.
	 * Checks, if policy evaluator is still on classpath - otherwise policy is disabled.
	 * 
	 * @param entityType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <E extends Identifiable> AuthorizationEvaluator<E> getEvaluator(IdmAuthorizationPolicyDto policy) {
		String evaluatorType = policy.getEvaluatorType();
		if (!evaluators.containsKey(evaluatorType)) {
			try {
				evaluators.put(evaluatorType, (AuthorizationEvaluator<?>) context.getBean(Class.forName(evaluatorType)));
			} catch (ClassNotFoundException | NoSuchBeanDefinitionException ex) {
				// disable removed evaluator classes
				LOG.warn("Evaluator type [{}] for policy [{}] not found. Policy will be disabled.", evaluatorType, policy.getId());
				policy.setDisabled(true);
				service.save(policy);
				return null;
			}
		}
		AuthorizationEvaluator<E> evaluator = (AuthorizationEvaluator<E>) evaluators.get(evaluatorType);
		if (evaluator.isDisabled()) {
			LOG.warn("Evaluator type [{}] for policy [{}] is disabled - policy will not be evaluated.", evaluatorType, policy.getId());
			return null;
		}
		//
		return evaluator;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<AuthorizationEvaluatorDto> getSupportedEvaluators() {
		List<AuthorizationEvaluatorDto> evaluators = new ArrayList<>();
		for(Entry<String, AuthorizationEvaluator> entry : context.getBeansOfType(AuthorizationEvaluator.class).entrySet()) {
			AuthorizationEvaluator<?> evaluator = entry.getValue();
			AuthorizationEvaluatorDto evaluatorDto = new AuthorizationEvaluatorDto();
			evaluatorDto.setEntityType(evaluator.getEntityClass().getCanonicalName());
			evaluatorDto.setEvaluatorType(evaluator.getClass().getCanonicalName());
			evaluatorDto.setModule(evaluator.getModule());
			evaluatorDto.setParameters(evaluator.getParameterNames());
			// resolve documentation
			evaluatorDto.setDescription(AutowireHelper.getBeanDescription(entry.getKey()));
			evaluators.add(evaluatorDto);
		}
		return evaluators;
	}

	@Override
	public Set<AuthorizableType> getAuthorizableTypes() {
		if (authorizableTypes == null) {
			authorizableTypes = new HashSet<>();
			// types with authorization evaluators support
			context.getBeansOfType(AuthorizableService.class).values().forEach(service -> {
				authorizableTypes.add(service.getAuthorizableType());
			});
			// add default - doesn't supports authorization evaluators
			moduleService.getAvailablePermissions().forEach(groupPermission -> {
				authorizableTypes.add(new AuthorizableType(groupPermission, null));
			});
		}
		return authorizableTypes;
	}
}
