package eu.bcvsolutions.idm.core.config.web;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;
import org.springframework.data.repository.query.spi.EvaluationContextExtensionSupport;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import eu.bcvsolutions.idm.core.api.repository.handler.UsernameAuditor;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.NotExportedAssociations;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.FlywayConfigCore;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;

/**
 * Web configurations - we are reusing spring data rest web configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Configuration
@EnableJpaAuditing
@AutoConfigureAfter({ FlywayConfigCore.class })
public class WebConfig extends RepositoryRestMvcConfiguration {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = corsConfiguration();
        // TODO: depends on FlywayConfigCore 
        // log.info("Starting with configurted allowed origins [{}]. Allowed origins could be changed through application setting.", config.getAllowedOrigins());
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
		return new RequestResourceResolver(defaultMessageConverters(), new DomainObjectReader(persistentEntities(), associationLinks()));
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
	 * Fills auditor for auditable entities
	 * 
	 * @return
	 */
	@Bean
	public AuditorAware<String> auditorProvider() {
		return new UsernameAuditor();
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
	 * Support hasAuthority etc. in search queries
	 * 
	 * @return
	 */
	@Bean
	public EvaluationContextExtension securityExtension() {
		return new EvaluationContextExtensionSupport() {
			@Override
			public String getExtensionId() {
				return "security";
			}

			@Override
			public SecurityExpressionRoot getRootObject() {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				return new SecurityExpressionRoot(authentication) {
				};
			}
		};
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
