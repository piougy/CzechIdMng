package eu.bcvsolutions.idm.core.api.config.cache;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

/**
 * Implementation of {@link IdMCacheConfiguration} which is used to define distributed caches. Note that if no distributed
 * cache server is configured, then this cache may be initialized as on-heap only.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public class DistributedIdMCacheConfiguration extends AbstractIdMCacheConfiguration{
	protected DistributedIdMCacheConfiguration(String cacheName, Class<? extends Serializable> keyType,
											   Class<? extends Serializable> valueType, ImmutableMap<String, Object> properties,
											   boolean onlyLocal, long size) {
		super(cacheName, keyType, valueType, properties, onlyLocal, size);
	}

	public static <K extends Serializable, V extends Serializable> DistributedIdMCacheConfiguration.Builder <K, V> builder() {
		return new DistributedIdMCacheConfiguration.Builder<>();
	}

	public static class Builder<K extends Serializable, V extends Serializable> extends AbstractIdMCacheConfiguration.Builder<K, V> {

		@Override
		public DistributedIdMCacheConfiguration build() {
			Assert.hasText(cacheName, "No cache name provided");
			Assert.notNull(keyType, "No key type provided");
			Assert.notNull(valueType, "No value type provided");
			//
			return new DistributedIdMCacheConfiguration(cacheName, keyType, valueType, ImmutableMap.copyOf(properties),
					false, size == null ? DEFAULT_HEAP_CACHE_SIZE : size);
		}
	}

}
