package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;

/**
 * Tree type filter - by text.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Tree type filter - by text. Search as \"like\" in name and code - lower, case insensitive.")
public class IdmTreeTypeTextFilter extends AbstractFilterBuilder<IdmTreeType, IdmTreeTypeFilter> {
	
	@Autowired
	public IdmTreeTypeTextFilter(IdmTreeTypeRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmTreeTypeFilter.PARAMETER_TEXT;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmTreeType> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmTreeTypeFilter filter) {
		if (StringUtils.isEmpty(filter.getText())) {
			return null;
		}	
		return builder.or(
				builder.like(builder.lower(root.get(IdmTreeType_.name)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(IdmTreeType_.code)), "%" + filter.getText().toLowerCase() + "%")					
				);
	}	
}