package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Common filter on codeable identifier.
 * Lookout - use only, when is needed, because additional entity read (select) is called.
 * 
 * @author Radek Tomiška
 */
@Component
public class CodeableFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	public static final String PROPERTY_NAME = DataFilter.PARAMETER_CODEABLE_IDENTIFIER;
	@Autowired private ApplicationContext context;
	private LookupService lookupService;
	
	@Override
	public String getName() {
		return PROPERTY_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (filter.getCodeableIdentifier() == null) {
			return null;
		}
		E entity = lookup(filter);
		if (entity == null) {
			// not found
			return builder.disjunction();
		}
		return builder.equal(root.get(AbstractEntity_.id), entity.getId());
	}	

	@Override	
	public Page<E> find(DataFilter filter, Pageable pageable) {
		if (filter.getCodeableIdentifier() == null) {
			return null;
		}
		E entity = lookup(filter);
		List<E> results = new ArrayList<>();
		if (entity != null) {
			results.add(entity);
		}
		return new PageImpl<>(results);
	}
	
	@SuppressWarnings("unchecked")
	private E lookup(DataFilter filter) {
		return (E) getLookupService().lookupEntity(filter.getDtoClass(), filter.getCodeableIdentifier());
	}
	
	/**
	 * Lazy init - lookup service needs filters to init itself
	 * @return
	 */
	private LookupService getLookupService() {
		if (lookupService == null) {
			lookupService = context.getBean(LookupService.class);
		}
		return lookupService;
	}
}
