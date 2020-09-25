package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Find assigned role by role catalogue code.
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Component(IdentityRoleByRoleCatalogueCodeFilter.FILTER_NAME)
@Description("Find assigned role by role catalogue code.")
public class IdentityRoleByRoleCatalogueCodeFilter extends AbstractFilterBuilder<IdmIdentityRole, IdmIdentityRoleFilter> {

	public static final String FILTER_NAME = "identity-role-by-role-catalogue-code-filter";
	public static final String PARAMETER_ROLE_CATALOGUE_CODE = "roleCatalogueCode";

	@Autowired
	public IdentityRoleByRoleCatalogueCodeFilter(IdmIdentityRoleRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return PARAMETER_ROLE_CATALOGUE_CODE;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentityRole> root, AbstractQuery<?> query, CriteriaBuilder cb, IdmIdentityRoleFilter filter) {
		String roleCatalogueCode = getParameterConverter().toString(filter.getData(), PARAMETER_ROLE_CATALOGUE_CODE);
		if (StringUtils.isEmpty(roleCatalogueCode)) {
			return null;
		}
		
		// Subquery for role catalogue role (intersection table).
		Subquery<IdmRoleCatalogueRole> roleCatalogueRoleSubquery = query.subquery(IdmRoleCatalogueRole.class);
		Root<IdmRoleCatalogueRole> subRootRoleCatalogueRole = roleCatalogueRoleSubquery.from(IdmRoleCatalogueRole.class);
		roleCatalogueRoleSubquery.select(subRootRoleCatalogueRole);

		// Join to role catalogue (we need code).
		Join<IdmRoleCatalogueRole, IdmRoleCatalogue> roleCatalogueJoin = subRootRoleCatalogueRole.join(IdmRoleCatalogueRole_.roleCatalogue);

		// query
		roleCatalogueRoleSubquery.where(
				cb.and(
						cb.equal(subRootRoleCatalogueRole.get(IdmRoleCatalogueRole_.role), root.get(IdmIdentityRole_.role)),
						cb.equal(roleCatalogueJoin.get(IdmRoleCatalogue_.code), roleCatalogueCode)
                )
		);

		// final exists
		return cb.exists(roleCatalogueRoleSubquery);
	}
}
