package eu.bcvsolutions.idm.core.api.config.cache;

import java.util.Map;

/**
 * Configuration of cache in CzechIdM. It contains basic attributes such as name, key and value types and size.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public interface IdMCacheConfiguration {

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

	Object getProperty(String propName);

	/**
	 * 
	 * @returnn Additional defined properties
	 */
	Map<String, Object> getProperties();

	/**
	 * This size is only used if local on-heap cache is used. Then it indicates number of entries that can be stored
	 * on heap in this cache.
	 *
	 * @return number of entries, which this cache can hold
	 */
	long getSize();
}
