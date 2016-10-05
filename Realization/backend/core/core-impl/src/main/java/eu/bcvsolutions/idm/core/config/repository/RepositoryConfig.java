package eu.bcvsolutions.idm.core.config.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;
import org.springframework.data.repository.query.spi.EvaluationContextExtensionSupport;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import eu.bcvsolutions.idm.core.api.repository.handler.UsernameAuditor;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.NotExportedAssociations;
import eu.bcvsolutions.idm.core.model.validator.IdmRoleValidator;

/**
 * Spring data rest configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Configuration
@EnableJpaAuditing
public class RepositoryConfig extends RepositoryRestMvcConfiguration {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Create a validator to use in bean validation - primary to be able to
	 * autowire without qualifier
	 */
	@Bean
	@Primary
	Validator validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	Validator roleValidator() {
		return new IdmRoleValidator();
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
		config.setReturnBodyForPutAndPost(true);
		// Only repositories annotated with @(Repository)RestResource are
		// exposed, unless their exported flag is set to false.
		config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);
	}

	@Override
	protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
		Validator validator = validator();
		Validator roleValidator = roleValidator();
		//
		validatingListener.addValidator("beforeCreate", validator);
		validatingListener.addValidator("beforeSave", validator);
		// TODO: register by annotations somehow ...
		validatingListener.addValidator("beforeCreate", roleValidator);
		validatingListener.addValidator("beforeSave", roleValidator);
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
	 * Could be used for manually resource projection
	 * 
	 * @return
	 */
	@Bean
	public SpelAwareProxyProjectionFactory projectionFactory() {
		return new SpelAwareProxyProjectionFactory();
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
