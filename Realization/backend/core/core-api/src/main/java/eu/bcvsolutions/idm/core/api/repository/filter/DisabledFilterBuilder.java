package eu.bcvsolutions.idm.core.api.repository.filter;

import java.util.Collections;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Filter by given property name is disabled => returns disjunction as predicate
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public class DisabledFilterBuilder<E extends BaseEntity> extends BaseFilterBuilder<E, DataFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DisabledFilterBuilder.class);
	//
	private final FilterBuilder<E, DataFilter> wrapped; // wrapped disabled filter property name
	
	public DisabledFilterBuilder(FilterBuilder<E, DataFilter> wrapped) {
		Assert.notNull(wrapped, "Disabled filter is required!");
		//
		this.wrapped = wrapped;
	}
	
	@Override
	public String getName() {
		return wrapped.getName();
	}
	
	@Override
	public boolean isDisabled() {
		return wrapped.isDisabled();
	}
	
	@Override
	public int getOrder() {
		return wrapped.getOrder();
	}

	@Override
	public String getConfigurationPrefix() {
		return wrapped.getConfigurationPrefix();
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return wrapped.getConfigurationService();
	}

	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (wrapped.getPredicate(root, query, builder, filter) != null) {
			LOG.debug("Filter [{}] is filled, but is disabled. Returning 'disjunction' predicate.", getName());
			//
			return builder.disjunction();
		}
		// filter builder is not interested ~ it's not filled property name in given filter
		return null;
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		LOG.debug("Filter [{}] is disabled. Returning empty page.", getName());
		//
		return new PageImpl<>(Collections.<E>emptyList());
	}
	

}
