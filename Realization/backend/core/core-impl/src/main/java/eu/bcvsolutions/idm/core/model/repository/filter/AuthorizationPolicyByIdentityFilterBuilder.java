package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;

/**
 * Authorization policy assigned by given identity by assigned or default role.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Component
@Description("Authorization policy assigned by given identity by assigned or default role.")
public class AuthorizationPolicyByIdentityFilterBuilder extends AbstractFilterBuilder<IdmAuthorizationPolicy, IdmAuthorizationPolicyFilter> {

	@Autowired @Lazy private RoleConfiguration roleConfiguration;
	@Autowired @Lazy private IdmRoleCompositionService roleCompositionService;
	
	@Autowired
	public AuthorizationPolicyByIdentityFilterBuilder(IdmAuthorizationPolicyRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return IdmAuthorizationPolicyFilter.PARAMETER_IDENTITY_ID;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmAuthorizationPolicy> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmAuthorizationPolicyFilter filter) {
		UUID identityId = filter.getIdentityId();
		if (identityId == null) {
			return null;
		}
		
		//
		// assigned role subquery
		Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
		Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
		subquery.select(subRoot);
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), identityId),
                		builder.equal(subRoot.get(IdmIdentityRole_.role), root.get(IdmAuthorizationPolicy_.role)) // correlation
                		)
        );
		Predicate predicate = builder.exists(subquery);
		//
		// or default role
		UUID defaultRoleId = roleConfiguration.getDefaultRoleId();
		if (defaultRoleId == null) {
			// default role is not defined
			return predicate;
		}
		//
		// find all default role sub roles 
		Set<UUID> defaultRoles = Sets.newHashSet(defaultRoleId);
		defaultRoles.addAll(
				roleCompositionService
					.findAllSubRoles(defaultRoleId)
					.stream()
					.map(IdmRoleCompositionDto::getSub)
					.collect(Collectors.toSet())
		);
		return builder.or(
				predicate,
				root.get(IdmAuthorizationPolicy_.role).get(IdmRole_.id).in(defaultRoles)
		);
	}
}
