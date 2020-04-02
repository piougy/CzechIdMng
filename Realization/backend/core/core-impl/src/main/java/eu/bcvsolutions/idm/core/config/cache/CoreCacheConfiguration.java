package eu.bcvsolutions.idm.core.config.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.cache.DistributedIdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.LocalIdMCacheConfiguration;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultGroovyScriptService;
import groovy.lang.Script;

/**
 * Configuration of caches used by core module
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Configuration
public class CoreCacheConfiguration {

	/**
	 * Define distributed cache for {@link eu.bcvsolutions.idm.core.api.service.ConfigurationService}
	 *
	 * @return IdMCacheConfiguration for {@link eu.bcvsolutions.idm.core.api.service.ConfigurationService}
	 */
	@Bean
	public IdMCacheConfiguration configurationServiceCacheConfiguration() {
		return DistributedIdMCacheConfiguration.<String, String> builder()
			.withName(DefaultConfigurationService.CACHE_NAME)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

	/**
	 * Define local only cache for {@link DefaultGroovyScriptService}. The reason, why we use local only cache
	 * is {@link Script} class, which this service caches, is not {@link java.io.Serializable} so it cannot
	 * be shared in distributed cache.
	 *
	 * @return IdMCacheConfiguration for {@link DefaultGroovyScriptService}
	 */
	@Bean
	public IdMCacheConfiguration groovyScriptCacheConfiguration() {
		return LocalIdMCacheConfiguration.<String, Script>builder()
				.withName(DefaultGroovyScriptService.CACHE_NAME)
				.withKeyType(String.class)
				.withValueType(Script.class)
				.build();
	}

}
