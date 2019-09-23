package eu.bcvsolutions.idm.core.model.repository.filter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.filter.DisabledFilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

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
	//
	@Autowired private ConfigurationService configurationService;
	@Autowired private EnabledEvaluator enabledEvaluator;

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
	
	@Override
	public <E extends BaseEntity> FilterBuilder<E, DataFilter> getBuilder(Class<E> entityClass, String propertyName) {
		FilterKey key = new FilterKey(entityClass, propertyName);
		//
		return getBuilder(key);
	}

	@SuppressWarnings("unchecked")
	private <E extends BaseEntity> FilterBuilder<E, DataFilter> getBuilder(FilterKey key) {
		if (!builders.hasPluginFor(key)) {
			return null;
		}
		//
		// default plugin by ordered definition
		FilterBuilder<E, DataFilter> builder = (FilterBuilder<E, DataFilter>) builders.getPluginFor(key);
		if (builder.isDisabled()) {
			return new DisabledFilterBuilder<E>(builder);
		}
		String implName = builder.getConfigurationValue(ConfigurationService.PROPERTY_IMPLEMENTATION);
		if (StringUtils.isEmpty(implName)) {
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
						"propertyName", key.getName(),
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<FilterBuilderDto> find(FilterBuilderFilter filter) {
		Set<FilterKey> registeredFilterKeys = new HashSet<>();
		List<FilterBuilderDto> dtos = new ArrayList<>();
		//
		// registered filter builders
		Map<String, FilterBuilder> filterBuilders = context.getBeansOfType(FilterBuilder.class);
		filterBuilders.forEach((beanName, filterBuilder) -> {
			if (enabledEvaluator.isEnabled(filterBuilder)) {
				FilterBuilderDto dto = toDto(filterBuilder);
				if (passFilter(dto, filter)) {
					FilterKey filterKey = new FilterKey(dto.getEntityClass(), dto.getName());
					// evaluate effective filter
					if (getBuilder(filterKey) != filterBuilder) { // check reference to instance
						dto.setDisabled(true);
					}
					registeredFilterKeys.add(filterKey);
					dtos.add(dto);
				}
			}
		});
		//
		// find field by recursion from registered services and filter dtos
		Map<String, ReadDtoService> services = context.getBeansOfType(ReadDtoService.class);
		services.forEach((beanName, service) -> {
			if (service.getEntityClass() == null) {
				LOG.trace("Service [{}], [{}] does not define controller entity, skip to resolve available filter criteria.",
						beanName, service.getClass());
			} else {
				for (Field declaredField : service.getFilterClass().getDeclaredFields()) {
					Class entityClass = service.getEntityClass();
					String propertyName = declaredField.getName();
					if (Modifier.isStatic(declaredField.getModifiers())) {
						try {
							Object propertyValue = declaredField.get(null);
							if (propertyValue instanceof String) {
								propertyName = (String) propertyValue;
							} else {
								LOG.trace("Value of static property [{}] for class [{}] is not string, skip to resolve available filter criteria.",
										propertyName, entityClass);
								continue;
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							LOG.warn("Get value of static property [{}] for class [{}] failed, skip to resolve available filter criteria.",
									propertyName, entityClass);
							continue;
						}
					}
					//
					FilterKey filterKey = new FilterKey(entityClass, propertyName);
					if (registeredFilterKeys.contains(filterKey)) {
						LOG.trace("Property [{}] for class [{}] has their own filter builder implemented.",
								propertyName, entityClass);
						continue;
					}
					//
					FilterBuilderDto dto = new FilterBuilderDto();
					// service and property is unique combination
					dto.setId(String.format("%s-%s", beanName, propertyName));
					dto.setName(propertyName);
					// service - can be overriden - https://wiki.czechidm.com/tutorial/dev/override_service
					dto.setModule(EntityUtils.getModule(service.getClass()));
					dto.setDescription("Internal service implementation (toPredicates).");
					dto.setEntityClass(entityClass);
					dto.setFilterClass(service.getFilterClass());
					if (service instanceof AbstractFormValueService<?, ?>) { // eav value services are constructed dynamically (prevent to show cglib class)
						dto.setFilterBuilderClass(AbstractFormValueService.class);
					} else {
						dto.setFilterBuilderClass(AutowireHelper.getTargetClass(service));
					}
					dto.setDisabled(false); // service is always effective filter
					//
					if (passFilter(dto, filter)) {
						dtos.add(dto);
					}
				}
			}
		});
		// sort by name
		dtos.sort(Comparator.comparing(AbstractComponentDto::getName));
		//
		LOG.debug("Returning [{}] registered filterBuilders", dtos.size());
		//
		return dtos;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public FilterBuilder<? extends BaseEntity, ? extends DataFilter> getFilterBuilder(String filterBuilderId) {
		Assert.notNull(filterBuilderId, "Filter builder identifier is required.");
		//
		return (FilterBuilder<? extends BaseEntity, ? extends DataFilter>) context.getBean(filterBuilderId);
	}
	
	@Override
	public void enable(String filterBuilderId) {
		Assert.notNull(filterBuilderId, "Filter builder identifier is required.");
		//
		FilterBuilder<? extends BaseEntity, ? extends DataFilter> filterBuilder = getFilterBuilder(filterBuilderId);
		Assert.notNull(filterBuilder, "Filter builder is required.");
		//
		// default plugin by ordered definition
		FilterBuilder<? extends BaseEntity, ? extends DataFilter> defaultBuilder = (FilterBuilder<? extends BaseEntity, ? extends DataFilter>)
				builders.getPluginFor(new FilterKey(filterBuilder.getEntityClass(), filterBuilder.getName()));
		// impl property is controlled by default filter configuration
		configurationService.setValue(
				defaultBuilder.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION),
				filterBuilderId);
	}
	
	/**
	 * Returns true, when given filterBuilder pass given filter
	 *
	 * @param filterBuilder
	 * @param filter
	 * @return
	 */
	protected boolean passFilter(FilterBuilderDto filterBuilder, FilterBuilderFilter filter) {
		Assert.notNull(filterBuilder, "Filter builder is requred to evaluate filter.");
		//
		if (filter == null || filter.getData().isEmpty()) {
			// empty filter
			return true;
		}
		// id
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering filter builder by [id] is not supported.");
		}
		// text - not supported
		String text = filter.getText();
		if (!StringUtils.isEmpty(text)) {
			text = text.toLowerCase();
			String filterBuilderDescription = filterBuilder.getDescription();
			String filterBuilderClassCanonicalName = filterBuilder.getFilterBuilderClass().getCanonicalName();
			if (!filterBuilder.getName().toLowerCase().contains(text.toLowerCase())
					&& (filterBuilderDescription == null || !filterBuilderDescription.toLowerCase().contains(text))
					&& !filterBuilder.getEntityClass().getCanonicalName().toLowerCase().contains(text)
					&& (
							filterBuilderClassCanonicalName == null // anonymous classes returns null.
							|| 
							!filterBuilderClassCanonicalName.toLowerCase().contains(text)
						)
					) {
				return false;
			}
		}
		// description - like
		String description = filter.getDescription();
		if (!StringUtils.isEmpty(description)) {
			String filterBuilderDescription = filterBuilder.getDescription();
			if (filterBuilderDescription == null || !filterBuilderDescription.toLowerCase().contains(description.toLowerCase())) {
				return false;
			}
		}
		// filter builders name
		String name = filter.getName();
		if (!StringUtils.isEmpty(name) && !name.equals(filterBuilder.getName())) {
			return false;
		}
		// module module
		String module = filter.getModule();
		if (!StringUtils.isEmpty(module) && !module.equals(filterBuilder.getModule())) {
			return false;
		}
		// entity class
		String entityClass = filter.getEntityClass();
		if (!StringUtils.isEmpty(entityClass) 
				&& !filterBuilder.getEntityClass().getCanonicalName().equals(entityClass)) {
			return false;
		}
		// filter class
		String filterBuilderClass = filter.getFilterBuilderClass();
		if (!StringUtils.isEmpty(filterBuilderClass)) {
			String filterBuilderClassCanonicalName = filterBuilder.getFilterBuilderClass().getCanonicalName();
			if (filterBuilderClassCanonicalName == null || !filterBuilderClassCanonicalName.equals(filterBuilderClass)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Convert filterBuilder to dto.
	 *
	 * @param key
	 * @param filterBuilder
	 * @return
	 */
	private FilterBuilderDto toDto(FilterBuilder<? extends BaseEntity, ? extends DataFilter> filterBuilder) {
		FilterBuilderDto dto = new FilterBuilderDto();
		dto.setId(filterBuilder.getId());
		dto.setName(filterBuilder.getName());
		dto.setModule(filterBuilder.getModule());
		dto.setDisabled(filterBuilder.isDisabled());
		dto.setDescription(filterBuilder.getDescription());
		dto.setEntityClass(filterBuilder.getEntityClass());
		dto.setFilterClass(filterBuilder.getFilterClass());
		dto.setFilterBuilderClass(AutowireHelper.getTargetClass(filterBuilder));
		return dto;
	}
}
