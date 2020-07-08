package eu.bcvsolutions.idm.core.config.cache;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.domain.CacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.domain.SerializableCacheObjectWrapper;

/**
 * This configuration sets up {@link CacheManager} for distributed caching using EhCache and Terracota server.
 * It is invoked before {@link ClusteredEhCacheConfiguration} so that there is only one {@link CacheManager}.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Configuration
@Order(1)
public class ClusteredEhCacheConfiguration {

	public static final String TERRACOTA_URL_PROPERTY = "cache.terracota.url";
	public static final String TERRACOTA_RESOURCE_NAME_PROPERTY = "cache.terracota.resource.name";
	public static final String TERRACOTA_RESOURCE_POOL_NAME_PROPERTY = "cache.terracota.resource.pool.name";
	public static final String TERRACOTA_RESOURCE_POOL_SIZE_PROPERTY = "cache.terracota.resource.pool.size";

	/**
	 * Defines clustered {@link CacheManager} using Terracotta server.
	 *
	 * @param terracotaUrl a list of IP addresses with ports (IP_ADDR:PORT)
	 * @param terracotaResourceName name of server resource to connect
	 * @param terracotaResourcePoolName name od server resource pool name
	 * @param terracotaResourcePoolSize size of server resource pool in MB
	 * @param idMCacheConfigurations a list of {@link IdMCacheConfiguration} defined in container
	 * @return CacheManager with distributed capabilities
	 */
	@Bean
	@Qualifier("jCacheManager")
	@ConditionalOnProperty(value = TERRACOTA_URL_PROPERTY)
	@ConditionalOnMissingBean
	public CacheManager ehCacheManager(@Value("${" + TERRACOTA_URL_PROPERTY + "}") String terracotaUrl,
								@Value("${" + TERRACOTA_RESOURCE_NAME_PROPERTY + "}") String terracotaResourceName,
								@Value("${" + TERRACOTA_RESOURCE_POOL_NAME_PROPERTY + "}") String terracotaResourcePoolName,
								@Value("${" + TERRACOTA_RESOURCE_POOL_SIZE_PROPERTY + "}") int terracotaResourcePoolSize,
								@Autowired List<IdMCacheConfiguration> idMCacheConfigurations) {
		CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
				CacheManagerBuilder.newCacheManagerBuilder()
						.with(ClusteringServiceConfigurationBuilder
								.cluster(parseServerAddresses(terracotaUrl), "default")
								.autoCreate(server -> server
										.defaultServerResource(terracotaResourceName)
										.resourcePool(terracotaResourcePoolName, terracotaResourcePoolSize, MemoryUnit.MB, terracotaResourceName)))
						// Set serializers for null value wrappers
						.withSerializer(CacheObjectWrapper.class, CacheWrapperSerializer.class)
						.withSerializer(SerializableCacheObjectWrapper.class, SerializableCacheWrapperSerializer.class);
		PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(true);

		// create caches using IdMCacheConfiguration instances
		if (!CollectionUtils.isEmpty(idMCacheConfigurations)) {
			for (IdMCacheConfiguration config : idMCacheConfigurations) {
				cacheManager.createCache(config.getCacheName(), toConcreteConfiguration(config, terracotaResourcePoolName));
			}
		}

		// get CacheManager (Jcache) with above updated configuration
		final EhcacheCachingProvider ehcacheCachingProvider = (EhcacheCachingProvider) Caching.getCachingProvider();
		return ehcacheCachingProvider.getCacheManager(ehcacheCachingProvider.getDefaultURI(), cacheManager.getRuntimeConfiguration());
	}

	private Iterable<InetSocketAddress> parseServerAddresses(String terracotaUrl) {
		String[] split = terracotaUrl.split(",");
		return Arrays.stream(split)
				.map(s ->
						new InetSocketAddress(
								s.split(":")[0],
								Integer.parseInt(s.split(":")[1]))
				).collect(Collectors.toList());
	}

	private CacheConfiguration<?, ?> toConcreteConfiguration(IdMCacheConfiguration idmCacheConfiguration, String teracotaResourcePoolName) {
		if (idmCacheConfiguration.isOnlyLocal()) {
			return CacheConfigurationBuilder
					.newCacheConfigurationBuilder(
							idmCacheConfiguration.getKeyType(),
							CacheObjectWrapper.class,
							getResourcePools(idmCacheConfiguration, teracotaResourcePoolName))
					.withValueSerializer(CacheWrapperSerializer.class)
					.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(idmCacheConfiguration.getTtl()))
					.build();
		} else {
			return CacheConfigurationBuilder
					.newCacheConfigurationBuilder(
							idmCacheConfiguration.getKeyType(), 
							SerializableCacheObjectWrapper.class,
							getResourcePools(idmCacheConfiguration, teracotaResourcePoolName))
					.withValueSerializer(SerializableCacheWrapperSerializer.class)
					.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(idmCacheConfiguration.getTtl()))
					.build();
		}
	}

	private ResourcePoolsBuilder getResourcePools(IdMCacheConfiguration idMCacheConfiguration, String teracotaResourcePoolName) {
		if (idMCacheConfiguration.isOnlyLocal()) {
			return ResourcePoolsBuilder.heap(idMCacheConfiguration.getSize());
		} else {
			return ResourcePoolsBuilder.heap(idMCacheConfiguration.getSize())
					.with(ClusteredResourcePoolBuilder.clusteredShared(teracotaResourcePoolName));
		}
	}

}
