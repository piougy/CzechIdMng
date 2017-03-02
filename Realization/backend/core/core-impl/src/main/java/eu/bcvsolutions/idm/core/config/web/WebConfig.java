package eu.bcvsolutions.idm.core.config.web;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.NotExportedAssociations;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.FlywayConfigCore;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;
import eu.bcvsolutions.idm.core.model.domain.Embedded;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleDto;

/**
 * Web configurations - we are reusing spring data rest web configuration
 * 
 * TODO: its not only web configuration ...
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@AutoConfigureAfter({ FlywayConfigCore.class })
public class WebConfig extends RepositoryRestMvcConfiguration {

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public FilterRegistrationBean corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = corsConfiguration();
		// TODO: depends on FlywayConfigCore
		// log.info("Starting with configurted allowed origins [{}]. Allowed
		// origins could be changed through application setting.",
		// config.getAllowedOrigins());
		config.setAllowCredentials(Boolean.TRUE);
		config.addAllowedHeader("*");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration(BaseEntityController.BASE_PATH + "/**", config);
		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	/**
	 * Cors configuration
	 * 
	 * @return
	 */
	@Bean
	public CorsConfiguration corsConfiguration() {
		return new DynamicCorsConfiguration();
	}

	/**
	 * Whether to use suffix pattern match (".*") when matching patterns to
	 * requests. If enabled a method mapped to "/users" also matches to
	 * "/users.*".
	 * <p>
	 * By default this is set to {@code true}.
	 * 
	 * @see #registeredSuffixPatternMatch
	 */
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// enable encoded slash in path parameters
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		urlPathHelper.setUrlDecode(false);
		configurer.setUrlPathHelper(urlPathHelper);
		//
		// disable extension suffixes
		configurer.setUseSuffixPatternMatch(Boolean.FALSE);
		//
		// this will be useful in future ...
		// configurer.setUseRegisteredSuffixPatternMatch(true);
	}

	/**
	 * Common json error response attributes
	 * 
	 * @return
	 */
	@Bean
	public ErrorAttributes errorAttributes() {
		return new RestErrorAttributes();
	}

	@Bean
	public RequestResourceResolver requestResourceResolver() {
		return new RequestResourceResolver(defaultMessageConverters(),
				new DomainObjectReader(persistentEntities(), associationLinks()));
	}

	/**
	 * Create a validator to use in bean validation - primary to be able to
	 * autowire without qualifier
	 */
	@Bean
	@Primary
	Validator validator() {
		return new LocalValidatorFactoryBean();
	}

	/**
	 * Adds joda time json module
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = super.objectMapper();
		mapper.registerModule(new JodaModule());
		return mapper;
	}

	@SuppressWarnings("unchecked")
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modeler = new ModelMapper();

		// Convert BaseEntity to UIID (get ID)
		Converter<? extends BaseEntity, UUID> entityToUiid = new Converter<BaseEntity, UUID>() {

			@Override
			public UUID convert(MappingContext<BaseEntity, UUID> context) {
				if (context != null && context.getSource() != null && context.getSource().getId() instanceof UUID) {
					MappingContext<?, ?> parentContext = context.getParent();
					if (parentContext != null && parentContext.getDestination() != null
							&& AbstractDto.class.isAssignableFrom(parentContext.getDestinationType())
							&& parentContext.getSource() != null
							&& BaseEntity.class.isAssignableFrom(parentContext.getSourceType())) {

						try {
							AbstractDto parentDto = (AbstractDto) parentContext.getDestination();
							BaseEntity entity = (BaseEntity) context.getSource();
							Map<String, AbstractDto> embedded = parentDto.getEmbedded();
							
							
							PropertyMapping propertyMapping = (PropertyMapping)context.getMapping();
							// Find name of field by property mapping
							String field = propertyMapping.getLastDestinationProperty().getName();
							// Find field in DTO class
							Field fieldTyp = parentContext.getDestinationType().getDeclaredField(field);
							if(fieldTyp.isAnnotationPresent(Embedded.class)){
								Embedded embeddedAnnotation = fieldTyp.getAnnotation(Embedded.class);
								if(embeddedAnnotation.enabled()){
									// If has field Embedded (enabled) annotation, then we will create new
									// instance of DTO
									AbstractDto dto = embeddedAnnotation.dtoClass().newInstance();
									dto.setTrimmed(true);
									// Separate map entity to new embedded DTO
									modeler.map(entity, dto);
									embedded.put(field, dto);
									// Add filled DTO to embedded map to parent DTO
									parentDto.setEmbedded(embedded);
								}
							}
						} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
							throw new CoreException(e);
						}
					}
					return (UUID) context.getSource().getId();
				}
				return null;
			}
		};

		// Condition for property ... if is property list and dto is trimmed,
		// then will be not used (set null)
		// or if is property list and have parent dto, then will be to set null
		// (only two levels are allowed).
		Condition<Object, Object> trimmListCondition = new Condition<Object, Object>() {

			@Override
			public boolean applies(MappingContext<Object, Object> context) {
				if (List.class.isAssignableFrom(context.getDestinationType())) {
					MappingContext<?, ?> parentContext = context.getParent();
					MappingContext<?, ?> superContext = parentContext != null ? parentContext.getParent() : null;

					if (superContext != null) {
						if (parentContext != null && parentContext.getDestination() instanceof AbstractDto) {
							((AbstractDto) parentContext).setTrimmed(true);
						}
						return false;
					}
					if (parentContext != null && parentContext.getDestination() instanceof AbstractDto
							&& ((AbstractDto) parentContext.getDestination()).isTrimmed()) {
						return false;
					}
				}
				return true;
			}

		};

		modeler.getConfiguration().setPropertyCondition(trimmListCondition);

		// entity to uiid converter will be set for all entities
		entityManager.getMetamodel().getEntities().forEach(entityType -> {
			if (entityType.getJavaType() == null) {
				return;
			}
			@SuppressWarnings("rawtypes")
			TypeMap typeMapEntityToUiid = modeler.createTypeMap(entityType.getJavaType(), UUID.class);
			typeMapEntityToUiid.setConverter(entityToUiid);
		});

		return modeler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.rest.webmvc.config.
	 * RepositoryRestMvcConfiguration
	 * #configureRepositoryRestConfiguration(org.springframework.data.rest.core.
	 * config.RepositoryRestConfiguration)
	 */
	@Override
	protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		// we like ids ...
		entityManager.getMetamodel().getEntities().forEach(entityType -> {
			config.exposeIdsFor(entityType.getJavaType());
		});
		// conventional base api endpoint. TODO: define version here?
		config.setBasePath(BaseEntityController.BASE_PATH);
		// it will be usefull for some clients (e.g. for putting new / updated
		// resource to client storage - redux etc.)
		config.setReturnBodyForPutAndPost(Boolean.TRUE);
		// Only repositories annotated with @(Repository)RestResource are
		// exposed, unless their exported flag is set to false.
		config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);
	}

	/**
	 * JSR-303 only for now
	 */
	@Override
	protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
		Validator validator = validator();
		validatingListener.addValidator("beforeCreate", validator);
		validatingListener.addValidator("beforeSave", validator);
	}

	/**
	 * We want to assemble embedded object to not exported repositories too.
	 */
	@Bean
	@Override
	public Associations associationLinks() {
		return new NotExportedAssociations(resourceMappings(), config());
	}
}
