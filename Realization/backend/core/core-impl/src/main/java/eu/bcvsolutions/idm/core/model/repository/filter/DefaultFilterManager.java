package eu.bcvsolutions.idm.core.model.repository.filter;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import eu.bcvsolutions.idm.core.api.repository.filter.DisabledFilterBuilder;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFilterManager.class);

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
		FilterBuilder<E, DataFilter> builder = (FilterBuilder<E, DataFilter>) builders.getPluginFor(new FilterKey(entityClass, propertyName));
		if (builder.isDisabled()) {
			return new DisabledFilterBuilder<E>(builder);
		}
		String implName = builder.getConfigurationValue(ConfigurationService.PROPERTY_IMPLEMENTATION);
		if (!StringUtils.hasLength(implName)) {
			// return default builder - configuration is empty
			return builder;
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

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
				// TODO: deprecated in 9.7.0, but fix this after original method will be removed => all custom filters can use original
				return filterBuilder.getPredicate(root, query, builder, filter);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/**
	 * Returns all registered filter builders
	 *
	 * @param filter
	 * @return
	 */
	@Override
	public List<FilterBuilderDto> find(FilterBuilderFilter filter) {
		List<FilterBuilderDto> dtos = new ArrayList<>();

		Map<String, FilterBuilder> filterBuilders = context.getBeansOfType(FilterBuilder.class);
		filterBuilders.forEach((key, filterBuilder) -> {
			FilterBuilderDto dto = toDto(key, filterBuilder);
			if (passFilter(dto, filter)) {
				dtos.add(dto);
			}
		});
		// sort by name
		dtos.sort(Comparator.comparing(AbstractComponentDto::getName));

		LOG.debug("Returning [{}] registered filterBuilders", dtos.size());
		//
		return dtos;
	}


	/**
	 * Convert filterBuilder to dto.
	 *
	 * @param key
	 * @param filterBuilder
	 * @return
	 */
	private FilterBuilderDto toDto(String key, FilterBuilder filterBuilder) {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setId(key);
		dto.setName(filterBuilder.getName());
		dto.setModule(filterBuilder.getModule());
		dto.setDisabled(filterBuilder.isDisabled());
		dto.setDescription(filterBuilder.getDescription());
		dto.setEntityClass(filterBuilder.getEntityClass());
		dto.setEntityType(filterBuilder.getEntityClass().getSimpleName());
		dto.setFilterBuilderClass(AutowireHelper.getTargetType(filterBuilder));
		//Text Not supported.
		//Id Not supported.
		return dto;
	}

	/**
	 * Returns true, when given filterBuilder pass given filter
	 *
	 * @param filterBuilder
	 * @param filter
	 * @return
	 */
	private boolean passFilter(FilterBuilderDto filterBuilder, FilterBuilderFilter filter) {
		if (filter == null || filter.getData().isEmpty()) {
			// empty filter
			return true;
		}
		// id
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering filter builder by [id] is not supported.");
		}
		// text - not supported
		if (!StringUtils.isEmpty(filter.getText())) {
			throw new UnsupportedOperationException("Filtering filter builder by [test] is not supported.");
		}
		// filter builders name
		if (!StringUtils.isEmpty(filter.getName()) && !filterBuilder.getName().equals(filter.getName())) {
			return false;
		}
		// module module
		if (!StringUtils.isEmpty(filter.getModule()) && !filter.getModule().equals(filterBuilder.getModule())) {
			return false;
		}
		// entity class
		if (!StringUtils.isEmpty(filter.getFilterBuilderClass()) && !filterBuilder.getFilterBuilderClass().contains(filter.getFilterBuilderClass())) {
			return false;
		}
		// description - like
		if (null != filter.getDescription()) {
			return null != filterBuilder.getDescription() && filterBuilder.getDescription().contains(filter.getDescription());
		}
		return true;
	}
}
