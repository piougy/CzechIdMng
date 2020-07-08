package eu.bcvsolutions.idm.core.api.config.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * This class contains basic configuration options for caches in CzechIdM. It is used to define Spring beans, which
 * will then serve as a template for constructing caches.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
public abstract class AbstractIdMCacheConfiguration implements IdMCacheConfiguration {

	public static final long DEFAULT_HEAP_CACHE_SIZE = Long.MAX_VALUE; // ~ unlimited by default. Use different strategy, if needed.

	private final String cacheName;
	private final Class<?> keyType;
	private final Class<?>  valueType;
	private final ImmutableMap<String, Object> properties;
	private final boolean onlyLocal;
	private final long size;
	private final Duration ttl;

	protected AbstractIdMCacheConfiguration(
			String cacheName, 
			Class<?> keyType, 
			Class<?> valueType, 
			ImmutableMap<String, Object> properties, 
			boolean onlyLocal, 
			long size) {
		this(cacheName, keyType, valueType, properties, onlyLocal, size, null);
	}
	
	protected AbstractIdMCacheConfiguration(
			String cacheName, 
			Class<?> keyType, 
			Class<?> valueType, 
			ImmutableMap<String, Object> properties, 
			boolean onlyLocal, 
			long size,
			Duration ttl) {
		this.cacheName = cacheName;
		this.keyType = keyType;
		this.valueType = valueType;
		this.properties = properties;
		this.onlyLocal = onlyLocal;
		this.size = size;
		this.ttl = ttl == null ? INFINITE_TTL : ttl;
	}

	@Override
	public boolean isOnlyLocal() {
		return onlyLocal;
	}

	@Override
	public String getCacheName() {
		return this.cacheName;
	}

	@Override
	public Class<?> getKeyType() {
		return this.keyType;
	}

	@Override
	public Class<?> getValueType() {
		return this.valueType;
	}

	@Override
	public Object getProperty(String propName) {
		return this.properties.get(propName);
	}

	/**
	 * Additional properties that may be used by some cache providers. It returns an {@link ImmutableMap}, so do not try
	 * to modify this map.
	 *
	 * @return ImmutableMap of additional configuration properties
	 */
	@Override
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@Override
	public long getSize() {
		return size;
	}
	
	@Override
	public Duration getTtl() {
		return ttl;
	}

	/**
	 * Builder for cache configuration. It ensures, that ll required configuration options are set before creating
	 * cache configuration.
	 *
	 * @param <K> key type used to limit which types of keys are used in this cache
	 * @param <V> value type used to limit type of stored values
	 */
	public abstract static class Builder<K, V> {

		protected String cacheName;
		protected Class<? extends K> keyType;
		protected Class<? extends V>  valueType;
		protected final Map<String, Object> properties = new HashMap<>();
		protected Long size = null;
		protected Duration ttl = null;

		public Builder<K, V> withName(final String name) {
			this.cacheName = name;
			return this;
		}

		public Builder<K, V> withKeyType(final Class<? extends K> keyType) {
			this.keyType = keyType;
			return this;
		}

		public Builder<K, V> withValueType(final Class<? extends V> valueType) {
			this.valueType = valueType;
			return this;
		}

		public Builder<K, V> withProperty(final String propName, final Object propValue) {
			this.properties.put(propName, propValue);
			return this;
		}

		public Builder<K, V> witchCacheSize(long size) {
			this.size = size;
			return this;
		}
		
		public Builder<K, V> withTtl(Duration ttl) {
			this.ttl = ttl;
			return this;
		}

		public abstract AbstractIdMCacheConfiguration build();

	}

}
