package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class DefaultIdmCacheManager implements IdmCacheManager {

    private final CacheManager springCacheManager;

    @Autowired
    public DefaultIdmCacheManager(CacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
    }

    @Override
    public Page<IdmCacheDto> getAllAvailableCaches() {
        Collection<String> cacheNames = springCacheManager.getCacheNames();
        List<IdmCacheDto> caches = cacheNames.stream().map(springCacheManager::getCache).map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(caches);
    }

    @Override
    public void evictCache(String cacheId) {
        Cache cache = springCacheManager.getCache(cacheId);
        cache.clear();
    }

    private IdmCacheDto toDto(Cache cache) {
        final IdmCacheDto result = new IdmCacheDto();
        final Object nativeCache = cache.getNativeCache();

        if (nativeCache instanceof ConcurrentHashMap) {
            ConcurrentHashMap concurrentHashMap = (ConcurrentHashMap) nativeCache;
            result.setSize(concurrentHashMap.size());
        }

        result.setId(cache.getName());
        result.setName(cache.getName());

        return result;
    }

}
