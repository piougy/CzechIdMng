package eu.bcvsolutions.idm.acc.security.evaluator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permissions to accounts by identity
 * 
 * @author Svanda
 *
 */
@Component
@Description("Permissions to accounts by identity")
public class AccountByIdentityEvaluator extends AbstractAuthorizationEvaluator<AccAccount> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private AuthorizationManager authorizationManager;
	@Autowired
	private LookupService lookupService;

	@Override
	public Set<String> getPermissions(AccAccount authorizable, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(authorizable, policy);
		if (authorizable == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		AccIdentityAccountFilter identityAccountsFilter = new AccIdentityAccountFilter();
		identityAccountsFilter.setAccountId(authorizable.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountsFilter, null)
				.getContent();

		identityAccounts.forEach(identityAccount -> {
			BaseEntity identity = lookupService.lookupEntity(IdmIdentity.class, identityAccount.getIdentity());
			permissions.addAll(authorizationManager.getPermissions(identity));
		});
		return permissions;
	}
	
	/**
	 * Returns transitive authorities by identity
	 */
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		// evaluates authorities on owner type class
		return authorizationManager.getAuthorities(identityId, IdmIdentity.class);
	}

	@Override
	public Predicate getPredicate(Root<AccAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			AuthorizationPolicy policy, BasePermission... permission) {
		UUID currentId = securityService.getCurrentId();
		if (!hasAuthority(currentId, policy, permission) || !securityService.isAuthenticated()) {
			return null;
		}

		// System identity account subquery
		Subquery<AccIdentityAccount> subquery = query.subquery(AccIdentityAccount.class);
		Root<AccIdentityAccount> subRoot = subquery.from(AccIdentityAccount.class);
		subquery.select(subRoot);

		// Identity subquery
		Subquery<IdmIdentity> subqueryIdentity = query.subquery(IdmIdentity.class);
		Root<IdmIdentity> subRootIdentity = subqueryIdentity.from(IdmIdentity.class);
		subqueryIdentity.select(subRootIdentity);

		// Subselect for identity
		subqueryIdentity
				.where(builder.and(authorizationManager.getPredicate(subRootIdentity, query, builder, permission),
						builder.equal(subRootIdentity, subRoot.get(AccIdentityAccount_.identity))));

		// Subselect for identity account
		subquery.where(builder.and( //
				builder.equal(root, subRoot.get(AccIdentityAccount_.account)), builder.exists(subqueryIdentity)));
		//
		return builder.exists(subquery);
	}
}
