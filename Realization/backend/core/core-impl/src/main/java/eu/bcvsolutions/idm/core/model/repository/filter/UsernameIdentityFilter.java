package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter by identity username.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Filter by identity username")
public class UsernameIdentityFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	@Autowired
	public UsernameIdentityFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_USERNAME;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		String username = filter.getUsername();
		if (StringUtils.isEmpty(username)) {
			return null;
		}
		return builder.equal(root.get(IdmIdentity_.username), username);
	}
}