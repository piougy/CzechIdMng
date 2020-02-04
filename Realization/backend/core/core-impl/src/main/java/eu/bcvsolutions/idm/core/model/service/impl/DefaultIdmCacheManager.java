package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;

/**
 * Default implementation of {@link IdmCacheManager}. It provides basic operations on cache such as listing available
 * caches and clearing them. Internally, this manager uses Spring's implementation {@link CacheManager}.
 *
 * Note that only ConcurrentHashMap implementation is supported at the time. If other implementation of cache is used,
 * then size of each cache will not be determined correctly.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component("idmCacheManager")
public class DefaultIdmCacheManager implements IdmCacheManager {

    private final CacheManager springCacheManager;

    @Autowired
    public DefaultIdmCacheManager(CacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
    }

    @Override
    public Page<IdmCacheDto> getAllAvailableCaches() {
        Collection<String> cacheNames = springCacheManager.getCacheNames();
        List<IdmCacheDto> caches = Optional.ofNullable(cacheNames).orElse(Collections.emptyList()).stream()
                .map(springCacheManager::getCache)
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(caches);
    }

    @Override
    public void evictCache(String cacheId) {
        // if we do not check cache existence prior asking Spring's CacheManager, then it is going to create it for us, which
        // is not what we want
        if (springCacheManager.getCacheNames().contains(cacheId)) {
            Cache cache = springCacheManager.getCache(cacheId);
            cache.clear();
        }
    }

    @Override
    public void evictAllCaches() {
        Optional.ofNullable(getAllAvailableCaches()).orElse(Page.empty())
                .getContent().stream()
                .map(IdmCacheDto::getId)
                .forEach(this::evictCache);
    }

    @Override
    public boolean cacheValue(String cacheName, Object key, Object value) {
        Assert.hasText(cacheName, "Cache name cannot be empty");
        Assert.notNull(key, "Cache key cannot be empty");
        // lazily creates cache if it does not exist yet
        final Cache cache = springCacheManager.getCache(cacheName);
        // need to check this in case caching is disabled
        if (cache != null) {
            cache.put(key, value);
        }
        return cache != null && Objects.equals(cache.get(key), value);
    }

    @Override
    public Cache.ValueWrapper getValue(String cacheName, Object key) {
        Assert.hasText(cacheName, "Cache name cannot be empty");
        Assert.notNull(key, "Cache key cannot be empty");
        //
        Cache cache = springCacheManager.getCache(cacheName);
        return cache == null ? null : cache.get(key);
    }

    @Override
    public void evictValue(String cacheName, Object key) {
        Assert.hasText(cacheName, "Cache name cannot be empty");
        Assert.notNull(key, "Cache key cannot be empty");
        //
        if (springCacheManager.getCacheNames().contains(cacheName)) {
            springCacheManager.getCache(cacheName).evict(key);
        }
    }


    private IdmCacheDto toDto(Cache cache) {
        final IdmCacheDto result = new IdmCacheDto();
        final Object nativeCache = cache.getNativeCache();
        // TODO: Add support for other cache implementations
        if (nativeCache instanceof ConcurrentHashMap) {
            final ConcurrentHashMap concurrentHashMap = (ConcurrentHashMap) nativeCache;
            result.setSize(concurrentHashMap.size());
        }
        //
        result.setId(cache.getName());
        result.setName(cache.getName());
        //
        return result;
    }

}
