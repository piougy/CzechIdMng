package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.UUID;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Filter roles by role guarantee.
 * 
 * @author Radek Tomiška
 * @since 9.7.0 - refactored from role service only
 */
@Component(RoleByGuaranteeFilter.BEAN_NAME)
@Description("Filter roles by role guarantee.")
public class RoleByGuaranteeFilter  extends AbstractFilterBuilder<IdmRole, IdmRoleFilter> {
	
	public static final String BEAN_NAME = "role-by-guarantee-filter";
	
	@Autowired
	public RoleByGuaranteeFilter(IdmRoleRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmRoleFilter.PARAMETER_GUARANTEE;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmRole> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmRoleFilter filter) {
		UUID guaranteeId = filter.getGuaranteeId();
		if (guaranteeId == null) {
			return null;
		}
		// guarante by identity
		Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
		Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
		subquery.select(subRoot);
	
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmRoleGuarantee_.role), root), // correlation attr
                		builder.equal(subRoot.get(IdmRoleGuarantee_.guarantee).get(IdmIdentity_.id), guaranteeId)
                		)
        );
		// guarantee by role - identity has assigned role
		Subquery<UUID> subqueryIdentityRole = query.subquery(UUID.class);
		Root<IdmIdentityRole> subRootIdentityRole = subqueryIdentityRole.from(IdmIdentityRole.class);
		subqueryIdentityRole.select(subRootIdentityRole.get(IdmIdentityRole_.role).get(IdmRole_.id));
		subqueryIdentityRole.where(
                builder.and(
                		builder.equal(subRootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), guaranteeId),
                		RepositoryUtils.getValidPredicate(subRootIdentityRole, builder)
                		)
        );
		//
		Subquery<IdmRoleGuaranteeRole> subqueryRole = query.subquery(IdmRoleGuaranteeRole.class);
		Root<IdmRoleGuaranteeRole> subRootRole = subqueryRole.from(IdmRoleGuaranteeRole.class);
		subqueryRole.select(subRootRole);
	
		subqueryRole.where(
                builder.and(
                		builder.equal(subRootRole.get(IdmRoleGuaranteeRole_.role), root), // correlation attr
                		subRootRole.get(IdmRoleGuaranteeRole_.guaranteeRole).get(IdmRole_.id).in(subqueryIdentityRole)
                		)
        );
		return builder.or(
				builder.exists(subquery),
				builder.exists(subqueryRole)
			);
	}

}
