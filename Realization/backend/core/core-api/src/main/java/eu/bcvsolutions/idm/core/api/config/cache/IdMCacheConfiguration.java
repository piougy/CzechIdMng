package eu.bcvsolutions.idm.core.api.config.cache;

import java.time.Duration;
import java.util.Map;

import org.ehcache.expiry.ExpiryPolicy;

/**
 * Configuration of cache in CzechIdM. It contains basic attributes such as name, key and value types and size.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
public interface IdMCacheConfiguration {
	
	/**
	 * A {@link Duration duration} that represents an infinite time.
	 * @since 10.4.1
	 */
	Duration INFINITE_TTL = ExpiryPolicy.INFINITE;

	/**
	 * This property indicates whether this cache should be only cached locally, or if it could be distributed.
	 * Note that caches which are distributed must use only {@link java.io.Serializable} keys and values.
	 *
	 * @return true if this cached should be only local, false otherwise
	 */
	boolean isOnlyLocal();

	/**
	 *
	 * @return Name of the cache
	 */
	String getCacheName();

	/**
	 *
	 * @return Type of the key stored in this cache
	 */
	Class<?> getKeyType();

	/**
	 *
	 * @return Type of values supported by this cache
	 */
	Class<?> getValueType();

	/**
	 * Additional cache properties.
	 * 
	 * @param propName
	 * @return
	 */
	Object getProperty(String propName);

	/**
	 * Additional defined properties.
	 * 
	 * @return additional defined properties
	 */
	Map<String, Object> getProperties();

	/**
	 * This size is only used if local on-heap cache is used. Then it indicates number of entries that can be stored
	 * on heap in this cache.
	 *
	 * @return number of entries, which this cache can hold
	 */
	long getSize();
	
	/**
	 * Cache entry expiration.
	 * A {@link Duration duration} that represents an infinite time by default, never returns {@code null}.
	 * 
	 * @return time to live duration.
	 * @since 10.4.1
	 */
	default Duration getTtl() {
		return INFINITE_TTL;
	}
}
