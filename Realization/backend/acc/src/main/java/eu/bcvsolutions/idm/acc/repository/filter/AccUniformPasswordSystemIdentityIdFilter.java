package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordSystemRepository;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccUniformPasswordSystemDto} filter by {@link IdmIdentityDto} and theirs
 * {@link AccIdentityAccountDto} - {@link AccAccountDto}. Filter returns all
 * uniform password system for accounts that own specified identity.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Uniform password system filter - identity id (equals)")
public class AccUniformPasswordSystemIdentityIdFilter
		extends AbstractFilterBuilder<AccUniformPasswordSystem, AccUniformPasswordSystemFilter> {

	@Autowired
	public AccUniformPasswordSystemIdentityIdFilter(AccUniformPasswordSystemRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return AccUniformPasswordSystemFilter.PARAMETER_IDENTITY_ID;
	}

	@Override
	public Predicate getPredicate(Root<AccUniformPasswordSystem> root, AbstractQuery<?> query, CriteriaBuilder builder, AccUniformPasswordSystemFilter filter) {
		if (filter.getIdentityId() == null) {
			return null;
		}

		Subquery<AccIdentityAccount> subqueryIdentityAccount = query.subquery(AccIdentityAccount.class);
		Root<AccIdentityAccount> subRootIdentityAccount = subqueryIdentityAccount.from(AccIdentityAccount.class);
		subqueryIdentityAccount.select(subRootIdentityAccount);

		Subquery<AccAccount> subqueryAccount = query.subquery(AccAccount.class);
		Root<AccAccount> subRootAccount = subqueryAccount.from(AccAccount.class);
		subqueryAccount.select(subRootAccount);
				
		Predicate exists = builder.exists(
				subqueryAccount.where(
						builder.and(
								builder.equal(subRootAccount.get(AccAccount_.system), root.get(AccUniformPasswordSystem_.system)), // corelation
								builder.exists(
										subqueryIdentityAccount.where(
												builder.and(
														builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.account), subRootAccount), // corelation
														builder.equal(subRootIdentityAccount.get(AccIdentityAccount_.identity).get(AbstractEntity_.id), filter.getIdentityId())
														)
												)
										)
								)
						)
				);
				
		return exists;
	}
}