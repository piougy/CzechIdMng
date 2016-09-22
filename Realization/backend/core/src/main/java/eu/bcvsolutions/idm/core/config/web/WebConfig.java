package eu.bcvsolutions.idm.core.config.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.config.PersistentEntityResourceAssemblerArgumentResolver;
import org.springframework.data.rest.webmvc.config.PersistentEntityResourceHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.impl.FlywayConfigCore;
import eu.bcvsolutions.idm.core.exception.RestErrorAttributes;
import eu.bcvsolutions.idm.core.model.domain.NotExportedAssociations;
import eu.bcvsolutions.idm.core.model.domain.PersistentEntityResolver;
import eu.bcvsolutions.idm.core.rest.BaseEntityController;

/**
 * Web configurations
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Configuration
@AutoConfigureAfter({ FlywayConfigCore.class })
public class WebConfig extends WebMvcConfigurerAdapter {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private SelfLinkProvider linkProvider;
	
	@Autowired
	private PersistentEntities persistentEntities; 
	
	@Autowired
	private PersistentEntityResourceHandlerMethodArgumentResolver persistentEntityResourceHandlerMethodArgumentResolver;
	
	@Autowired
	private List<HttpMessageConverter<?>> messageConverters;
	
	@Autowired
	private RepositoryRestConfiguration config;
	
	@Autowired
	private Associations associationLinks;

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
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(persistentEntityResourceHandlerMethodArgumentResolver);
		
		SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
		projectionFactory.setBeanFactory(applicationContext);
		projectionFactory.setResourceLoader(applicationContext);
		
		PersistentEntityResourceAssemblerArgumentResolver peraResolver = new PersistentEntityResourceAssemblerArgumentResolver(
				persistentEntities, linkProvider, config.getProjectionConfiguration(), projectionFactory,
				associationLinks);
		
		argumentResolvers.add(peraResolver);
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.addAll(messageConverters);
	}
	
	@Bean
	public PersistentEntityResolver persistentEntityResolver() {
		return new PersistentEntityResolver(messageConverters, new DomainObjectReader(persistentEntities, associationLinks));
	}
}
