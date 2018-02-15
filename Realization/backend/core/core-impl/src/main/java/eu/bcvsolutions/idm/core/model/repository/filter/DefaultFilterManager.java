package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Builds filters to domain types.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFilterManager implements FilterManager {
	
	private final ApplicationContext context;
	private final PluginRegistry<FilterBuilder<?, ?>, FilterKey> builders;
	
	@Autowired
	public DefaultFilterManager(
			ApplicationContext context,
			List<? extends FilterBuilder<?, ?>> builders) {
		Assert.notNull(context);
		Assert.notNull(builders, "Filter builders are required");
		//
		this.context = context;
		this.builders = OrderAwarePluginRegistry.create(builders);
	}

	@SuppressWarnings("unchecked")
	public <E extends BaseEntity> FilterBuilder<E, DataFilter> getBuilder(Class<E> entityClass, String propertyName) {
		FilterKey key = new FilterKey(entityClass, propertyName);
		if (!builders.hasPluginFor(key)) {
			return null;
		}
		//
		// default plugin by ordered definition
		FilterBuilder<?, ?> builder = builders.getPluginFor(new FilterKey(entityClass, propertyName));
		String implName = builder.getConfigurationValue(ConfigurationService.PROPERTY_IMPLEMENTATION);
		if (!StringUtils.hasLength(implName)) {
			// return default builder - configuration is empty
			return (FilterBuilder<E, DataFilter>) builder;
		}
		//
		try {
			// returns bean by name from filter configuration
			return (FilterBuilder<E, DataFilter>) context.getBean(implName);
		} catch (Exception ex) {
			throw new ResultCodeException(
					CoreResultCode.FILTER_IMPLEMENTATION_NOT_FOUND, 
					ImmutableMap.of(
						"implementation", implName,
						"propertyName", propertyName,
						"configurationProperty", builder.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION)
						), ex);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Predicate> toPredicates(Root<? extends BaseEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		List<Predicate> predicates = new ArrayList<>();
		if (filter == null) {
			// empty filter - builders cannot be found
			return predicates;
		}
		Class<? extends BaseEntity> entityClass = root.getJavaType();
		return filter.getData().keySet() // all properties in filter (filled value is not checked here) 
			.stream()
			.map(propertyName -> {
				return (FilterBuilder) getBuilder(entityClass, propertyName);	
			})
			.filter(Objects::nonNull)
			.map(filterBuilder -> {
				return filterBuilder.getPredicate(root, query, builder, filter);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
}
