package eu.bcvsolutions.idm.example.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter by identity's username - search as \"like\" in username - case insensitive.
 * For example purpose, how to override core filters.
 * 
 * @author Radek Tomiška
 *
 */
@Component("exampleUsernameIdentityFilter")
@Description("Filter by identity's username - search as \"like\" in username - case insensitive")
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
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getUsername() == null) {
			return null;
		}
		return builder.like(builder.lower(root.get(IdmIdentity_.username)), "%" + filter.getUsername() + "%");
	}
	
	@Override
	public int getOrder() {
		// 0 => default, we don't want to override filter as default, but is possible when order is less than 0. 
		return 10;
	}
}