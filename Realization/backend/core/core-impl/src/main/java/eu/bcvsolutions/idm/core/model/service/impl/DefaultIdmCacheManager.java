package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.TierStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.domain.CacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.domain.SerializableCacheObjectWrapper;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;

/**
 * Default implementation of {@link IdmCacheManager}. It provides basic operations on cache such as listing available
 * caches and clearing them. Internally, this manager uses jCache JSR107 {@link CacheManager}
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
@Component("idmCacheManager")
public class DefaultIdmCacheManager implements IdmCacheManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmCacheManager.class);
	private static final String EMPTY_KEY_MSG = "Cache key cannot be empty";
    private static final String EMPTY_NAME_MSG = "Cache name cannot be empty";
	//
    private final CacheManager jCacheManager;
    private final Map<String, IdMCacheConfiguration> cacheConfigurations;
    private boolean sizeAvailable = true; // optimize loading cache size

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
        	cache.put(key, toCache);
        }
        return cache != null && Objects.equals(cache.get(key), toCache);
    }

    @Override
    public ValueWrapper getValue(String cacheName, Object key) {
        Assert.hasText(cacheName, EMPTY_NAME_MSG);
        Assert.notNull(key, EMPTY_KEY_MSG);
        //
        Cache<Object, Object> cache = jCacheManager.getCache(cacheName);      
        IdMCacheConfiguration cacheConfiguration = cacheConfigurations.get(cacheName);
        //
        if (cache == null) {
            return null;
        }
        //
        return toValueWrapper(cacheConfiguration, cache.get(key));
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
        IdmCacheDto dto = new IdmCacheDto();
        dto.setId(cache.getName());
        dto.setName(cache.getName());
        // There is no other way of determining which module this cache belongs to
        String[] split = StringUtils.split(cache.getName(), ":");
        dto.setModule(split != null && split.length > 0 ? split[0] : "");
        //
        if (sizeAvailable) {
	        try {
	        	// set size, when local ehcache is used
		        Field field = cache.getClass().getDeclaredField("statisticsBean");
		        field.setAccessible(true);
		        Object object = field.get(cache);
		        field = object.getClass().getDeclaredField("cacheStatistics");
		        field.setAccessible(true);
		        CacheStatistics statistics = (CacheStatistics) field.get(object);
		        //
		        dto.setSize(((TierStatistics) statistics.getTierStatistics().get("OnHeap")).getMappings());
		        //
		        
		        // TODO: cache size - return whole cache size for each item now ... how to fix it?
//		        field = cache.getClass().getDeclaredField("ehCache");
//		        field.setAccessible(true);
//		        Ehcache ehCache = (Ehcache) field.get(cache);
//		        //
//		        field = EhcacheBase.class.getDeclaredField("store");
//		        field.setAccessible(true);
//		        //
//		        Object store = field.get(ehCache);
//		        //
//		        if (store instanceof OnHeapStore) {
//		        	OnHeapStore heapStore = (OnHeapStore) store;
//		        	
//		        	ValueHolder valueHolder = heapStore.get(cache.getName());
//			        //
//			        SizeOf sizeOf = SizeOf.newInstance();
//			        //
//			        System.out.println(cache.getName() + ": " + FileUtils.byteCountToDisplaySize(sizeOf.deepSizeOf(heapStore)));
//		        }
		        
	        } catch (Exception ex) {
		        LOG.debug("Cache [{}] size is not available", cache.getName(), ex);
		        sizeAvailable = false;
		    }
    	}
        //
        return dto;
    }

    private boolean isConfigLocalOnly(IdMCacheConfiguration configuration) {
        return configuration == null || configuration.isOnlyLocal();
    }

}
