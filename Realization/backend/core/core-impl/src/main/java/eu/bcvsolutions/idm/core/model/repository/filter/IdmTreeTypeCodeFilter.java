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
 * Tree type filter - by code.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Tree type filter - by code (equals)")
public class IdmTreeTypeCodeFilter extends AbstractFilterBuilder<IdmTreeType, IdmTreeTypeFilter> {
	
	@Autowired
	public IdmTreeTypeCodeFilter(IdmTreeTypeRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmTreeTypeFilter.PARAMETER_CODE;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmTreeType> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmTreeTypeFilter filter) {
		if (StringUtils.isEmpty(filter.getCode())) {
			return null;
		}
		return builder.equal(root.get(IdmTreeType_.code), filter.getCode());
	}	
}