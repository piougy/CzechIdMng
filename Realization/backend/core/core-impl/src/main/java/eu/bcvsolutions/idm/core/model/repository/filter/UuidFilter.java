package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Common filter on entity uuid
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class UuidFilter implements FilterBuilder<AbstractEntity, DataFilter> {

	public static final String PROPERTY_NAME = "id";
	
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	
	@Override
	public Predicate getPredicate(Root<AbstractEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (filter.getId() == null) {
			return null;
		}
		return builder.equal(root.get(AbstractEntity_.id), filter.getId());
	}
	
	@Override
	public int getOrder() {
		return ConfigurationService.DEFAULT_ORDER;
	}

	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Override
	public boolean supports(FilterKey delimiter) {
		return delimiter.getName().equals(this.getName());
	}

	@Override
	public String getName() {
		return PROPERTY_NAME;
	}

	@Override
	public Page<AbstractEntity> find(DataFilter filter, Pageable pageable) {
		// TODO: dto lookup could be used ...
		// TODO: dto lookup refactoring - use repositories vs remove "plugin" interface from services 
		throw new UnsupportedOperationException("Find by uuid only is not supported, use dto lookup instead.");
	}
}
