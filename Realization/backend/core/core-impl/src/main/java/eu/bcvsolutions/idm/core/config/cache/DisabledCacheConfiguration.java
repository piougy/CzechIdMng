package eu.bcvsolutions.idm.core.config.cache;

import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * This configuration sets up {@link CacheManager} for in-memory caching using EhCache. It is invoked after
 * {@link ClusteredEhCacheConfiguration} so that there is only one {@link CacheManager} in context in case there
 * is a distributed cache set up.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Configuration
@Order(0)
public class DisabledCacheConfiguration {

	/**
	 * This cache manager does not store any data. It is a null object implementation of Java's {@link CacheManager}
	 * to use when caching is disabled using spring.cache.type=none property
	 *
	 * @return a dummy implementation of {@link CacheManager} to use, when caching is disabled
	 */
	@Bean
	@Qualifier("jCacheManager")
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "spring.cache.type", havingValue = "none")
	public CacheManager ehCacheManager() {
		return new NullCacheManager();
	}

}
