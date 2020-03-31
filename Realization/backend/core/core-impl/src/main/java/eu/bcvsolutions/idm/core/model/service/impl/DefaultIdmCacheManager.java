package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.core.api.config.cache.domain.CacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.domain.SerializableCacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;

/**
 * Default implementation of {@link IdmCacheManager}. It provides basic operations on cache such as listing available
 * caches and clearing them. Internally, this manager uses jCache JSR107 {@link CacheManager}
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component("idmCacheManager")
public class DefaultIdmCacheManager implements IdmCacheManager {

    private final CacheManager jCacheManager;

    private final Map<String, IdMCacheConfiguration> cacheConfigurations;

    private static final String EMPTY_KEY_MSG = "Cache key cannot be empty";
    private static final String EMPTY_NAME_MSG = "Cache name cannot be empty";

    @Autowired
    public DefaultIdmCacheManager(CacheManager springCacheManager, List<IdMCacheConfiguration> cacheConfigurations) {
        this.jCacheManager = springCacheManager;
        this.cacheConfigurations = Maps.uniqueIndex(cacheConfigurations, IdMCacheConfiguration::getCacheName);
    }

    @Override
    public List<IdmCacheDto> getAllAvailableCaches() {
        Iterable<String> cacheNames = jCacheManager.getCacheNames();
        return StreamSupport.stream(cacheNames.spliterator(), false)
                .map(jCacheManager::getCache)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void evictCache(String cacheId) {
        Cache<Object, Object> cache = jCacheManager.getCache(cacheId);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public void evictAllCaches() {
    	getAllAvailableCaches()
    			.stream()
                .map(IdmCacheDto::getId)
                .forEach(this::evictCache);
    }

    @Override
    public boolean cacheValue(String cacheName, Object key, Object value) {
        Assert.hasText(cacheName, EMPTY_NAME_MSG);
        Assert.notNull(key, EMPTY_KEY_MSG);
        //
        final Cache<Object, Object> cache = jCacheManager.getCache(cacheName);
        final IdMCacheConfiguration configuration = cacheConfigurations.get(cacheName);
        // We can cast here safely, because DistributedIdMCacheConfiguration only allows Serializable types
        final Object toCache = isConfigLocalOnly(configuration) ? new CacheObjectWrapper<>(value) : new SerializableCacheObjectWrapper<>((Serializable)value);
        if (cache != null) {
                cache.put(key,  toCache);
        }
        return cache != null && Objects.equals(cache.get(key), toCache);
    }

    @Override
    public Optional<Object> getValue(String cacheName, Object key) {
        Assert.hasText(cacheName, EMPTY_NAME_MSG);
        Assert.notNull(key, EMPTY_KEY_MSG);
        //
        final Cache<Object, Object> cache = jCacheManager.getCache(cacheName);
        final IdMCacheConfiguration cacheConfiguration = cacheConfigurations.get(cacheName);
        //
        if (cache == null) {
            return Optional.empty();
        }
        //
        ValueWrapper result = toValueWrapper(cacheConfiguration, cache.get(key));
        return result == null ? Optional.empty() : Optional.ofNullable(result.get());
    }

    @SuppressWarnings("unchecked")
    private ValueWrapper toValueWrapper(IdMCacheConfiguration cacheConfiguration, Object o) {
        if (isConfigLocalOnly(cacheConfiguration)) {
            return (CacheObjectWrapper<?>) o;
        } else {
            return (SerializableCacheObjectWrapper<Serializable>) o;
        }
    }

    @Override
    public void evictValue(String cacheName, Object key) {
        Assert.hasText(cacheName, EMPTY_NAME_MSG);
        Assert.notNull(key, EMPTY_KEY_MSG);
        //
        final Cache<Object, Object> cache = jCacheManager.getCache(cacheName);
        if (cache != null) {
            cache.remove(key);
        }
    }

    private IdmCacheDto toDto(Cache<Object, Object> cache) {
        final IdmCacheDto result = new IdmCacheDto();

        result.setId(cache.getName());
        result.setName(cache.getName());
        // There is no other way of determining which module this cache belongs to
        String[] split = StringUtils.split(cache.getName(), ":");
        result.setModule(split != null && split.length > 0 ? split[0] : "");
        //
        return result;
    }

    private boolean isConfigLocalOnly(IdMCacheConfiguration configuration) {
        return configuration == null || configuration.isOnlyLocal();
    }

}
