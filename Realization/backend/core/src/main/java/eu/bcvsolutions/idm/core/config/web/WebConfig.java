package eu.bcvsolutions.idm.core.config.web;

import java.util.List;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;

/**
 * Web configurations
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Configuration
public class WebConfig {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebConfig.class);

	@Value("#{'${allowed-origins:http://localhost:3000,http://localhost/idm}'.replaceAll(\"\\s*\",\"\").split(',')}")
	private List<String> allowedOrigins;

	@Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        allowedOrigins.forEach(allowedOrigin -> {
        	log.info("Adding allowed origin [{}]", allowedOrigin);
        	config.addAllowedOrigin(allowedOrigin);
        });
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
	
	//@Bean
    RoutesBuilder myRouter() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {            	
            	RouteDefinition route = from("timer://foo?period=10000");
            	route.transform().simple("ref:myBean");
            	route.to("bean:sftpStager?method=stage");
            	route.to("log:bar");
            }
        };
    }
	
	//@Bean
    String myBean() {
        return "First email";
    }
}
