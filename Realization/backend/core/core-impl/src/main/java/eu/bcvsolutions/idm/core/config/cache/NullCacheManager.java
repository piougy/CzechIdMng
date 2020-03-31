package eu.bcvsolutions.idm.core.config.cache;

import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

/**
 * Implementation of {@link CacheManager} interface, which does nothing. CzechIdM uses this implementation as a "null object"
 * pattern, when caching is turned off.
 *
 * @author Peter Štrunc
 */
public class NullCacheManager implements CacheManager {
	@Override
	public CachingProvider getCachingProvider() {
		return Caching.getCachingProvider();
	}

	@Override
	public URI getURI() {
		return getCachingProvider().getDefaultURI();
	}

	@Override
	public ClassLoader getClassLoader() {
		return Caching.getDefaultClassLoader();
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) {
		return null;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
		return null;
	}

	@Override
	public <K, V> Cache<K, V> getCache(String cacheName) {
		return null;
	}

	@Override
	public Iterable<String> getCacheNames() {
		return Collections.emptyList();
	}

	@Override
	public void destroyCache(String cacheName) {
		// Do nothing because this is just a null object for when caching is turned off
	}

	@Override
	public void enableManagement(String cacheName, boolean enabled) {
		// Do nothing because this is just a null object for when caching is turned off
	}

	@Override
	public void enableStatistics(String cacheName, boolean enabled) {
		// Do nothing because this is just a null object for when caching is turned off
	}

	@Override
	public void close() {
		// Do nothing because this is just a null object for when caching is turned off
	}

	@Override
	public boolean isClosed() {
		return true;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		return null;
	}
}
