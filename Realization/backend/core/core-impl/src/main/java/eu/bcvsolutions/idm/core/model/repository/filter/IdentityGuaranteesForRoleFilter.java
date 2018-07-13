package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter by identity's username
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Filter identities, which is guarantee for role (by identity and by role)")
public class IdentityGuaranteesForRoleFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	@Autowired
	public IdentityGuaranteesForRoleFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_GUARANTEES_FOR_ROLE;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		UUID guaranteesForRole = filter.getGuaranteesForRole();
		if (guaranteesForRole == null) {
			return null;
		}
		// guarantee for role can be defined as identity
		Subquery<IdmRoleGuarantee> subqueryIdentity = query.subquery(IdmRoleGuarantee.class);
		Root<IdmRoleGuarantee> subRootIdentity = subqueryIdentity.from(IdmRoleGuarantee.class);
		subqueryIdentity.select(subRootIdentity);
		subqueryIdentity.where(
                builder.and(
                		builder.equal(subRootIdentity.get(IdmRoleGuarantee_.role).get(IdmRole_.id), guaranteesForRole),
                		builder.equal(subRootIdentity.get(IdmRoleGuarantee_.guarantee), root) // corelation
                		)
        );
		// guarantee for role can be defined as identity with role assigned
		Subquery<UUID> subqueryRole = query.subquery(UUID.class);
		Root<IdmRoleGuaranteeRole> subRootRole = subqueryRole.from(IdmRoleGuaranteeRole.class);
		subqueryRole.select(subRootRole.get(IdmRoleGuaranteeRole_.guaranteeRole).get(IdmRole_.id));
		subqueryRole.where(
                builder.and(builder.equal(subRootRole.get(IdmRoleGuaranteeRole_.role).get(IdmRole_.id), guaranteesForRole))
        );
		Subquery<UUID> subqueryIdentityRole = query.subquery(UUID.class);
		Root<IdmIdentityRole> subRootIdentityRole = subqueryIdentityRole.from(IdmIdentityRole.class);
		subqueryIdentityRole.select(subRootIdentityRole.get(IdmIdentityRole_.role).get(IdmRole_.id));
		subqueryIdentityRole.where(
                builder.and(
                		builder.equal(subRootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation
                		RepositoryUtils.getValidPredicate(subRootIdentityRole, builder),
                		subRootIdentityRole.get(IdmIdentityRole_.role).get(IdmRole_.id).in(subqueryRole)
                		)
        );
		//
		return builder.or(builder.exists(subqueryIdentity), builder.exists(subqueryIdentityRole));
	}
}