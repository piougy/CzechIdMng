package eu.bcvsolutions.idm.example.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.entity.ExampleProduct;
import eu.bcvsolutions.idm.example.entity.ExampleProduct_;
import eu.bcvsolutions.idm.example.repository.ExampleProductRepository;

/**
 * Example product filter - by text.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Example product filter - by text. Search as \"like\" in name, code and description - lower, case insensitive.")
public class TextExampleProductFilter extends AbstractFilterBuilder<ExampleProduct, ExampleProductFilter> {
	
	@Autowired
	public TextExampleProductFilter(ExampleProductRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return ExampleProductFilter.PARAMETER_TEXT;
	}
	
	@Override
	public Predicate getPredicate(Root<ExampleProduct> root, CriteriaQuery<?> query, CriteriaBuilder builder, ExampleProductFilter filter) {
		if (StringUtils.isEmpty(filter.getText())) {
			return null;
		}	
		return builder.or(
				builder.like(builder.lower(root.get(ExampleProduct_.name)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(ExampleProduct_.code)), "%" + filter.getText().toLowerCase() + "%"),
				builder.like(builder.lower(root.get(ExampleProduct_.description)), "%" + filter.getText().toLowerCase() + "%")					
				);
	}
}