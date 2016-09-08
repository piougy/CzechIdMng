package eu.bcvsolutions.idm.core.config.web;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.impl.FlywayConfigCore;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;

/**
 * Web configurations
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Configuration
@AutoConfigureAfter({ FlywayConfigCore.class })
public class WebConfig {

	@Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = corsConfiguration();
        // TODO: depends on FlywayConfigCore 
        // log.info("Starting with configurted allowed origins [{}]. Allowed origins could be changed through application setting.", config.getAllowedOrigins());
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/api/**", config);
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
	 * Common json object mapper
	 * 
	 * @return
	 */
	@Bean
	public ObjectMapper jsonMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
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
}
