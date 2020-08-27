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
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem_;
import eu.bcvsolutions.idm.acc.entity.AccUniformPassword_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordSystemRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccUniformPasswordSystemDto} filter by {@link AccUniformPasswordDto} and their disabled attribute
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Uniform password system filter - disabled in uniform password")
public class AccUniformPasswordSystemDisabledFilter
		extends AbstractFilterBuilder<AccUniformPasswordSystem, AccUniformPasswordSystemFilter> {

	@Autowired
	public AccUniformPasswordSystemDisabledFilter(AccUniformPasswordSystemRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return AccUniformPasswordSystemFilter.PARAMETER_UNIFORM_PASSWORD_DISABLED;
	}

	@Override
	public Predicate getPredicate(Root<AccUniformPasswordSystem> root, AbstractQuery<?> query, CriteriaBuilder builder, AccUniformPasswordSystemFilter filter) {
		if (filter.getUniformPasswordDisabled() == null) {
			return null;
		}

		Subquery<AccUniformPassword> subquery = query.subquery(AccUniformPassword.class);
		Root<AccUniformPassword> subRoot = subquery.from(AccUniformPassword.class);
		subquery.select(subRoot);

		return builder.exists(
				subquery.where(
						builder.and(
								builder.equal(root.get(AccUniformPasswordSystem_.uniformPassword), subRoot), // corelation
								filter.getUniformPasswordDisabled()
								?
									builder.isTrue(subRoot.get(AccUniformPassword_.disabled))
									:
									builder.isFalse(subRoot.get(AccUniformPassword_.disabled))
							)
						)
				);
	}
}