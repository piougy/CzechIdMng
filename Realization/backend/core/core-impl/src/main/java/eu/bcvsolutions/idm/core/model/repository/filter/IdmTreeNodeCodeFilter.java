package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;

/**
 * Tree node filter - by code.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Tree node filter - by code (equals)")
public class IdmTreeNodeCodeFilter extends AbstractFilterBuilder<IdmTreeNode, IdmTreeNodeFilter> {
	
	@Autowired
	public IdmTreeNodeCodeFilter(IdmTreeNodeRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmTreeTypeFilter.PARAMETER_CODE;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmTreeNode> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmTreeNodeFilter filter) {
		if (StringUtils.isEmpty(filter.getCode())) {
			return null;
		}
		return builder.equal(root.get(IdmTreeNode_.code), filter.getCode());
	}	
}