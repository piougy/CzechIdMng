package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordRepository;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccUniformPasswordDto} filter by code
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Uniform password filter - by code (equals)")
public class AccUniformPasswordCodeFilter extends AbstractFilterBuilder<AccUniformPassword, AccUniformPasswordFilter> {
	
	@Autowired
	public AccUniformPasswordCodeFilter(AccUniformPasswordRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DataFilter.PARAMETER_CODEABLE_IDENTIFIER;
	}
	
	@Override
	public Predicate getPredicate(Root<AccUniformPassword> root, AbstractQuery<?> query, CriteriaBuilder builder, AccUniformPasswordFilter filter) {
		if (StringUtils.isEmpty(filter.getCodeableIdentifier())) {
			return null;
		}
		return builder.equal(root.get(AccUniformPassword_.code), filter.getCodeableIdentifier());
	}	
}