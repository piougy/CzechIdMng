package eu.bcvsolutions.idm.core.api.config.cache;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

/**
 * Implementation of {@link IdMCacheConfiguration} which is used to define local only caches.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public class LocalIdMCacheConfiguration extends AbstractIdMCacheConfiguration {

	protected LocalIdMCacheConfiguration(String cacheName, Class<?> keyType, Class<?> valueType, ImmutableMap<String, Object> properties, boolean onlyLocal, long size) {
		super(cacheName, keyType, valueType, properties, onlyLocal, size);
	}

	public static <K, V> LocalIdMCacheConfiguration.Builder <K, V> builder() {
		return new LocalIdMCacheConfiguration.Builder<>();
	}

	public static class Builder<K, V> extends AbstractIdMCacheConfiguration.Builder<K, V> {
		@Override
		public LocalIdMCacheConfiguration build() {
			Assert.hasText(cacheName, "No cache name provided");
			Assert.notNull(keyType, "No key type provided");
			Assert.notNull(valueType, "No value type provided");
			//
			return new LocalIdMCacheConfiguration(cacheName, keyType, valueType, ImmutableMap.copyOf(properties),
					true, size == null ? DEFAULT_HEAP_CACHE_SIZE : size);
		}
	}

}
