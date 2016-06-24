package eu.bcvsolutions.idm.core.config.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import eu.bcvsolutions.idm.core.model.repository.handler.UsernameAuditor;
import eu.bcvsolutions.idm.core.model.validator.IdmRoleValidator;

/**
 * Spring data rest configuration
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Configuration
@EnableJpaAuditing
public class RepositoryConfig extends RepositoryRestMvcConfiguration {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
     * Create a validator to use in bean validation - primary to be able to autowire without qualifier
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
     * @see org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration
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
        config.setBasePath("/api");
        // it will be usefull for some clients (e.g. for putting new / updated resource to client storage - redux etc.)
        config.setReturnBodyForPutAndPost(true);
        // Only repositories annotated with @(Repository)RestResource are exposed, unless their exported flag is set to false.
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
}
