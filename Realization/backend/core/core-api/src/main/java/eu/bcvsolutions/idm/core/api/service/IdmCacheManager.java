package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import org.springframework.data.domain.Page;

/**
 * Provides useful methods for working with cache in CzechIdM. Note that this manager does not provide methods for
 * managing cached values. To create new caches and storing/retrieving values in/from them use {@link org.springframework.cache.CacheManager}
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
     * Evict cache with given name. If cache with given name does not exist, then {@link eu.bcvsolutions.idm.core.api.exception.ResultCodeException}
     * will be thrown. Note that caches may be created lazily, so your cache may not be created in the time of calling
     * this method. To avoid errors, check that your cache is created using {@link IdmCacheManager#getAllAvailableCaches()}.
     *
     * @param cacheId Name of cache to evict
     * @throws eu.bcvsolutions.idm.core.api.exception.ResultCodeException If cache with given name does not exist
     */
    void evictCache(String cacheId);

    /**
     * Evicts all caches which are present in container at the time of method call. To determine which caches to evict
     * it uses {@link IdmCacheManager#getAllAvailableCaches()}
     */
    void evictAllCaches();
}
