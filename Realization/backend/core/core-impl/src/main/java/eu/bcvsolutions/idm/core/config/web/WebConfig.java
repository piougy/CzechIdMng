package eu.bcvsolutions.idm.core.config.web;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.geo.GeoModule;
import org.springframework.data.util.Lazy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.CoreFlywayConfig;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;

/**
 * Web configurations.
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@AutoConfigureAfter({ CoreFlywayConfig.class })
@EnableHypermediaSupport(type = HypermediaType.HAL)
@ImportResource("classpath*:META-INF/spring-data-rest/**/*.xml")
@Import({ SpringDataJacksonConfiguration.class, EnableSpringDataWebSupport.QuerydslActivator.class })
public class WebConfig extends HateoasAwareSpringDataWebConfiguration {

	@Autowired ApplicationContext applicationContext;
	@Autowired GeoModule geoModule;
	//
	private final Lazy<ObjectMapper> mapper;
	
	public WebConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
		super(context, conversionService);
		//
		this.mapper = Lazy.of(() -> {
			// idea of RepositoryRestMvcConfiguration
			ObjectMapper mapper = new ObjectMapper();
			//
			Jdk8Module jdk8Module = new Jdk8Module();
			jdk8Module.configureAbsentsAsNulls(true);
			mapper.registerModule(jdk8Module);
			mapper.registerModule(geoModule);
			mapper.registerModule(new ParameterNamesModule());
			mapper.registerModule(new JavaTimeModule());
			//
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false); // db (e.g. posgresql) does not save nanos
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false); // db (e.g. posgresql) does not save nanos
			//
			return mapper;
		});
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
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
		//
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
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
	 * Content negotiation
	 * - disable path extension, we want to support '.' in url path parameter.
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer
			.favorPathExtension(false) // disable path extension
			.favorParameter(true); // enable 'format' parameter usage
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
	
	@Bean
	public ObjectMapper objectMapper() {
		return mapper.get();
	}
}
