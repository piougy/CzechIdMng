package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.domain.Page;

/**
 * Provides useful methods for working with cache in CzechIdM. Note that this manager does not provide methods for
 * managing cached values. To create new caches and storing/retrieving values in/from them use {@link org.springframework.cache.CacheManager}
 *
 *
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public interface IdmCacheManager {

    /**
     * Returns all available caches. No filtering is possible. This method always returns all currently created caches.
     * Note that caches may be created lazily, so not all expected caches may be present in the result set.
     *
     * @return a list of {@link IdmCacheDto}
     */
    Page<IdmCacheDto> getAllAvailableCaches();

    /**
     * Evict cache with given name. If cache with given name does not exist, then this method does nothing.
     * Note that caches may be created lazily, so your cache may not be created in the time of calling
     * this method. To avoid errors, check that your cache is created using {@link IdmCacheManager#getAllAvailableCaches()}.
     *
     * @param cacheId Name of cache to evict
     */
    void evictCache(String cacheId);

    /**
     * Evicts all caches which are present in container at the time of method call. To determine which caches to evict
     * it uses {@link IdmCacheManager#getAllAvailableCaches()}
     */
    void evictAllCaches();

    /**
     * Stores value in cache. If there already was a value stored in the same cache under given key it is rewritten. If
     * cache with given name is not available, then it is created and value is then stored in it.
     *
     * @param cacheName Name of cache to store values
     * @param key Key under which given value will be stored in cache
     * @param value Value to store in cache
     * @throws IllegalArgumentException if cacheName or key arguments are null
     * @return true if value was successfully stored in cache, false otherwise
     */
    boolean cacheValue(String cacheName, Object key, Object value);

    /**
     * Retrieves value form cache with given name. If there is no cached record for given key, method returns null.
     * If there is a null value stored in cache for given key, then {@link SimpleValueWrapper} , which contains a null
     * value, is returned.
     *
     * @param cacheName Name of cache to search value in
     * @param key Key to search value in cache
     * @return SimpleValueWrapper containing stored value (can be null) if any value for given key is present, null otherwise
     * @throws IllegalArgumentException if cacheName or key arguments are null
     */
    Cache.ValueWrapper getValue(String cacheName, Object key);

    /**
     * Removes value from cache.
     *
     * @param cacheName Name of the cache from which should the value be removed
     * @param key Key, under which is removed value currently stored
     * @throws IllegalArgumentException if cacheName or key arguments are null
     */
    void evictValue(String cacheName, Object key);

}
