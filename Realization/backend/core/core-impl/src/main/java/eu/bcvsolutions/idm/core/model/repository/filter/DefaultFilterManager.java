package eu.bcvsolutions.idm.core.model.repository.filter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.FilterNotSupportedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.filter.DisabledFilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
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
	//
	// cache key / service or filter builder
	private Map<FilterKey, FilterBuilderDto> registeredServiceFilters = null;
	// ignored internal filter properties
	private final Set<String> ignoredFilterProperties = Sets.newHashSet(
			"parameterConverter", // parameter conversion
			"dtoClass", // filter type
			"sort", // TODO: load from configuration, but cannot be configured now anyway - FE is hard coded to this props too.
			"page", 
			"size"); 
	
	@Autowired
	public DefaultFilterManager(
			ApplicationContext context,
			List<? extends FilterBuilder<?, ?>> builders) {
		Assert.notNull(context, "Spring context is required.");
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
		//
		Class<? extends BaseEntity> entityClass = root.getJavaType();
		Set<String> filterProperties = filter.getData().keySet();
		for (String filterProperty : filterProperties) {
			FilterKey key = new FilterKey(entityClass, filterProperty);
			FilterBuilder filterBuilder = getBuilder(key);
			//
			if (filterBuilder == null) {
				if (ignoredFilterProperties.contains(filterProperty)) {
					LOG.trace("Pageable or internal property [{}] will be ignored by filters.", filterProperty);
					continue;
				}
				if (CollectionUtils.isEmpty(filter.getData().get(filterProperty))) {
					LOG.trace("Filter property [{}] is empty and will be ignored by filters.", filterProperty);
					continue;
				}
				// check property is processed by service
				if (!getRegisteredServiceFilters().containsKey(key) // by service definition
						&& !getRegisteredFilters(entityClass, filter.getClass()).containsKey(key)) { // by filter instance
					FilterNotSupportedException ex = new FilterNotSupportedException(key);
					//
					if (configurationService.getBooleanValue(
							PROPERTY_CHECK_SUPPORTED_FILTER_ENABLED, 
							DEFAULT_CHECK_SUPPORTED_FILTER_ENABLED)) {
						// throw exception otherwise => unrecognized filter is not supported
						throw ex;
					} else {
						// log exception only
						ExceptionUtils.log(LOG, ex);
					}
					
				}
				LOG.trace("Filter property [{}] for entity [{}] will by processed directly by service predicates.", filterProperty, entityClass.getSimpleName());
				continue;
			}
			Predicate predicate = filterBuilder.getPredicate(root, query, builder, filter);
			if (predicate != null) {
				predicates.add(predicate);
			}
		}
		//
		return predicates;
	}

	/**
	 * Returns all registered filter builders
	 *
	 * @param filter
	 * @return
	 */
	@Override
	public List<FilterBuilderDto> find(FilterBuilderFilter filter) {
		// sort by name
		return findFilters(filter)
				.values()
				.stream()
				.flatMap(registeredFilters -> registeredFilters.stream())
				.sorted(Comparator.comparing(AbstractComponentDto::getName))
				.collect(Collectors.toList());
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
	 * Registered filters - by service and filter builders.
	 * 
	 * @param filter search filters
	 * @return registered filters by given key
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Map<FilterKey, List<FilterBuilderDto>> findFilters(FilterBuilderFilter filter) {
		Map<FilterKey, List<FilterBuilderDto>> registeredFilters = new HashMap<>();
		//
		// registered filter builders
		// TODO: not possible to cache now => cache has to be evicted, when module is enabled / disabled.
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
					if (!registeredFilters.containsKey(filterKey)) {
						registeredFilters.put(filterKey, new ArrayList<>());
					}
					registeredFilters.get(filterKey).add(dto);
				}
			}
		});
		//
		// find field by recursion from registered services and filter dtos
		getRegisteredServiceFilters().forEach((filterKey, filterBuilder) -> {
			if (registeredFilters.containsKey(filterKey)) {
				LOG.trace("Property [{}] for class [{}] has their own filter builder implemented.",
						filterKey.getName(), filterKey.getEntityClass());
			} else if (passFilter(filterBuilder, filter)) {
				registeredFilters.put(filterKey, Lists.newArrayList(filterBuilder)); // just one => not registrable, not overridable
			}
		});
		//
		LOG.debug("Returning [{}] registered filterBuilders", registeredFilters.size());
		//
		return registeredFilters;
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
	
	/**
	 * Returns filters processed directly by service predicates.
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<FilterKey, FilterBuilderDto> getRegisteredServiceFilters() {
		if (registeredServiceFilters == null) {
			registeredServiceFilters = new HashMap<>();
			// find field by recursion from registered services and filter dtos
			Map<String, ReadDtoService> services = context.getBeansOfType(ReadDtoService.class);
			services.forEach((beanName, service) -> {
				Class<? extends BaseEntity> entityClass = service.getEntityClass();
				Class<? extends BaseFilter> filterClass = service.getFilterClass();
				if (entityClass == null || filterClass == null) {
					LOG.trace("Service [{}], [{}] does not define controlled entity [{}] or filter[{}], skip to resolve available filter criteria.",
							beanName, service.getClass(), entityClass, filterClass);
				} else {
					registeredServiceFilters.putAll(
							getRegisteredFilters(entityClass, filterClass)
								.entrySet()
								.stream()
								.collect(Collectors.toMap(
										e -> e.getKey(),
										e -> {
											FilterBuilderDto dto = e.getValue();
											// service and property is unique combination
											dto.setId(String.format("%s-%s", beanName, dto.getName()));
											// service - can be overriden - https://wiki.czechidm.com/tutorial/dev/override_service
											dto.setModule(EntityUtils.getModule(service.getClass()));
											//
											if (service instanceof AbstractFormValueService<?, ?>) { // eav value services are constructed dynamically (prevent to show cglib class)
												dto.setFilterBuilderClass(AbstractFormValueService.class);
											} else {
												dto.setFilterBuilderClass(AutowireHelper.getTargetClass(service));
											}
											return dto;
										}
						        ))
							);					
				}
			});
		}
		//
		return registeredServiceFilters;
	}
	
	private Map<FilterKey, FilterBuilderDto> getRegisteredFilters(Class<? extends BaseEntity> entityClass, Class<? extends BaseFilter> filterClass) {
		Assert.notNull(entityClass, "Entity class is required to resolve registered filters.");
		Assert.notNull(filterClass, "Filter class is required to resolve filter properties.");
		//
		Map<FilterKey, FilterBuilderDto> registeredServiceFilters = new HashMap<>();
		
		Class<?> processFilterClass = filterClass;
		while (!processFilterClass.equals(Object.class)) {
			Stream
				.of(processFilterClass.getDeclaredFields(), processFilterClass.getFields()) // both static and private field from interfaces and super classes
				.flatMap(Stream::of)
				.forEach(declaredField -> {
					
					String propertyName = declaredField.getName();
					if (Modifier.isStatic(declaredField.getModifiers())) {
						try {
							Object propertyValue = declaredField.get(null);
							if (propertyValue instanceof String) {
								propertyName = (String) propertyValue;
							} else {
								LOG.trace("Value of static property [{}] for class [{}] is not string, skip to resolve available filter criteria.",
										propertyName, filterClass);
								return;
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							LOG.warn("Get value of static property [{}] for class [{}] failed, skip to resolve available filter criteria.",
									propertyName, filterClass);
							return;
						}
					}
					if (ignoredFilterProperties.contains(propertyName)) {
						LOG.trace("Pageable or internal property [{}] will be ignored by filters.", propertyName);
						return;
					}
					FilterKey filterKey = new FilterKey(entityClass, propertyName);
					if (registeredServiceFilters.containsKey(filterKey)) {
						// already resolved e.g. by interface
						return;
					}
					//
					FilterBuilderDto dto = new FilterBuilderDto();
					dto.setName(propertyName);
					dto.setDescription("Internal service implementation (toPredicates).");
					dto.setEntityClass(entityClass);
					dto.setFilterClass(filterClass);
					dto.setDisabled(false); // service is always effective filter
					//
					registeredServiceFilters.put(filterKey, dto);
				});
			//
			processFilterClass = processFilterClass.getSuperclass();
		}
		//
		return registeredServiceFilters;
	}
	
}
