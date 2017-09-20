package eu.bcvsolutions.idm.vs.evaluator;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.entity.VsRequest_;
import eu.bcvsolutions.idm.vs.entity.VsSystemImplementer;
import eu.bcvsolutions.idm.vs.entity.VsSystemImplementer_;
import eu.bcvsolutions.idm.vs.repository.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemImplementerDto;

/**
 * Permissions to virtual system requests by identity (implementers)
 * 
 * @author Svanda
 *
 */
@Component
@Description("Permissions to virtual system requests by identity (implementers)")
public class VsRequestByImplementerEvaluator extends AbstractAuthorizationEvaluator<VsRequest> {

	private final SecurityService securityService;
	private final VsSystemImplementerService systemImplementerService;
	private final IdmIdentityRoleService identityRoleService;

	@Autowired
	public VsRequestByImplementerEvaluator(SecurityService securityService,
			VsSystemImplementerService systemImplementerService, IdmIdentityRoleService identityRoleService) {
		Assert.notNull(securityService);
		Assert.notNull(systemImplementerService);
		Assert.notNull(identityRoleService);
		//
		this.securityService = securityService;
		this.systemImplementerService = systemImplementerService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	public Set<String> getPermissions(VsRequest authorizable, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(authorizable, policy);
		if (authorizable == null || authorizable.getSystem() == null) {
			return permissions;
		}
		VsSystemImplementerFilter systemImplementerFilter = new VsSystemImplementerFilter();
		systemImplementerFilter.setSystemId(authorizable.getSystem().getId());
		List<VsSystemImplementerDto> implemnters = systemImplementerService.find(systemImplementerFilter, null)
				.getContent();

		UUID currentId = securityService.getCurrentId();

		// Find all valid roles (includes check on contract validity)
		Set<UUID> roles = identityRoleService.findValidRole(currentId, null).getContent()//
				.stream()//
				.map(IdmIdentityRoleDto::getRole)//
				.collect(Collectors.toSet());

		for (VsSystemImplementerDto implementer : implemnters) {
			if (implementer.getIdentity() != null && implementer.getIdentity().equals(currentId)) {
				permissions.addAll(policy.getPermissions());
				break;
			}
			if (implementer.getRole() != null && roles.contains(implementer.getRole())) {
				permissions.addAll(policy.getPermissions());
				break;
			}
		}
		return permissions;
	}

	@Override
	public Predicate getPredicate(Root<VsRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			AuthorizationPolicy policy, BasePermission... permission) {
		UUID currentId = securityService.getCurrentId();
		if (!hasAuthority(currentId, policy, permission)) {
			return null;
		}

		// Find all valid roles (includes check on contract validity)
		Set<UUID> roles = identityRoleService.findValidRole(currentId, null).getContent()//
				.stream()//
				.map(IdmIdentityRoleDto::getRole)//
				.collect(Collectors.toSet());

		// System implementer subquery
		Subquery<VsSystemImplementer> subquery = query.subquery(VsSystemImplementer.class);
		Root<VsSystemImplementer> subRoot = subquery.from(VsSystemImplementer.class);
		subquery.select(subRoot);
		subquery.where(builder.and( //
				builder.equal(root.get(VsRequest_.system), subRoot.get(VsSystemImplementer_.system)), //
				builder.or(// correlation attribute
						builder.equal(subRoot.get(VsSystemImplementer_.identity).get(IdmIdentity_.id), currentId),
						subRoot.get(VsSystemImplementer_.role).get(IdmRole_.id).in(roles))));
		//
		return builder.exists(subquery);
	}
}
