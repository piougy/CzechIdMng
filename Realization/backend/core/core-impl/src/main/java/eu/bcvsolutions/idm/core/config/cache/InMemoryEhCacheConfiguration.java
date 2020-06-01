package eu.bcvsolutions.idm.core.config.cache;

import java.util.List;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.core.api.config.cache.domain.CacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.domain.SerializableCacheObjectWrapper;

/**
 * This configuration sets up {@link CacheManager} for in-memory caching using EhCache. It is invoked after
 * {@link ClusteredEhCacheConfiguration} so that there is only one {@link CacheManager} in context in case there
 * is a distributed cache set up.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Configuration
@Order(2)
public class InMemoryEhCacheConfiguration {

	/**
	 * Defines in-memory cache manager.
	 *
	 * @param idMCacheConfigurations {@link List} of {@link IdMCacheConfiguration} defined in container
	 * @return CacheManager with on-heap capabilities
	 */
	@Bean
	@Qualifier("jCacheManager")
	@ConditionalOnMissingBean
	public CacheManager ehCacheManager(@Autowired List<IdMCacheConfiguration> idMCacheConfigurations) {
		CacheManagerBuilder<?> localCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();

		if (!CollectionUtils.isEmpty(idMCacheConfigurations)) {
			for (IdMCacheConfiguration config : idMCacheConfigurations) {
				localCacheManagerBuilder = localCacheManagerBuilder.withCache(config.getCacheName(), toConcreteConfiguration(config));
			}
		}

		// get CacheManager (Jcache) with above updated configuration
		final EhcacheCachingProvider ehcacheCachingProvider = (EhcacheCachingProvider) Caching.getCachingProvider();
		return ehcacheCachingProvider.getCacheManager(
				ehcacheCachingProvider.getDefaultURI(),
				localCacheManagerBuilder.build(true).getRuntimeConfiguration()
		);
	}

	private CacheConfiguration<?, ?> toConcreteConfiguration(IdMCacheConfiguration idMCacheConfiguration) {
		if (idMCacheConfiguration.isOnlyLocal()) {
			return CacheConfigurationBuilder.newCacheConfigurationBuilder(
					idMCacheConfiguration.getKeyType(), CacheObjectWrapper.class,
					ResourcePoolsBuilder.heap(idMCacheConfiguration.getSize())
			).withValueSerializer(CacheWrapperSerializer.class).build();
		} else {
			return CacheConfigurationBuilder.newCacheConfigurationBuilder(
					idMCacheConfiguration.getKeyType(), SerializableCacheObjectWrapper.class,
					ResourcePoolsBuilder.heap(idMCacheConfiguration.getSize())
			).withValueSerializer(SerializableCacheWrapperSerializer.class).build();
		}
	}

}
