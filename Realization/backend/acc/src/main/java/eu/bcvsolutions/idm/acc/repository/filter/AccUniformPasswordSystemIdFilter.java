package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordRepository;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccUniformPasswordDto} filter by {@link SysSystemDto} id.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Uniform password filter - system id (equals)")
public class AccUniformPasswordSystemIdFilter extends AbstractFilterBuilder<AccUniformPassword, AccUniformPasswordFilter> {
	
	@Autowired
	public AccUniformPasswordSystemIdFilter(AccUniformPasswordRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return AccUniformPasswordFilter.PARAMETER_SYSTEM_ID;
	}
	
	@Override
	public Predicate getPredicate(Root<AccUniformPassword> root, AbstractQuery<?> query, CriteriaBuilder builder, AccUniformPasswordFilter filter) {
		if (filter.getSystemId() == null) {
			return null;
		}

		Subquery<AccUniformPasswordSystem> subquery = query.subquery(AccUniformPasswordSystem.class);
		Root<AccUniformPasswordSystem> subRoot = subquery.from(AccUniformPasswordSystem.class);
		subquery.select(subRoot);
		subquery.where(
			builder.equal(subRoot.get(AccUniformPasswordSystem_.uniformPassword), root), // corelation
			builder.equal(subRoot.get(AccUniformPasswordSystem_.system).get(AbstractEntity_.id), filter.getSystemId())
		);
				
				
		return builder.exists(subquery);
	}	
}