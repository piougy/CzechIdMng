package eu.bcvsolutions.idm.core.config.web;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.domain.RequestResourceResolver;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.CoreFlywayConfig;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;

/**
 * Web configurations - we are reusing spring data rest web configuration,
 * but rest endpoins are exposed ourself
 * 
 * TODO: its not only web configuration ...
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@AutoConfigureAfter({ CoreFlywayConfig.class })
public class WebConfig extends RepositoryRestMvcConfiguration {

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
		source.registerCorsConfiguration(BaseDtoController.BASE_PATH + "/**", config);
		config.addExposedHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME);
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
	LocalValidatorFactoryBean validatorFactory() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	@Primary
	Validator validator() {
		return validatorFactory();
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

	/**
	 * JSR-303 only for now
	 */
	@Override
	protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
		Validator validator = validator();
		validatingListener.addValidator("beforeCreate", validator);
		validatingListener.addValidator("beforeSave", validator);
	}
}
