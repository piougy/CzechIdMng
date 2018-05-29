package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;

/**
 * Filter in external code and username. Use in statement.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class DefaultIdentityIdentifiersFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {

	@Autowired
	public DefaultIdentityIdentifiersFilter(BaseEntityRepository<IdmIdentity, ?> repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_IDENTIFIERS;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmIdentityFilter filter) {
		List<String> identifiers = filter.getIdentifiers();
		if (identifiers.isEmpty()) {
			return null;
		}
		//
		return builder.or(
				root.get(IdmIdentity_.externalCode).in(identifiers),
				root.get(IdmIdentity_.username).in(identifiers));
	}

}
