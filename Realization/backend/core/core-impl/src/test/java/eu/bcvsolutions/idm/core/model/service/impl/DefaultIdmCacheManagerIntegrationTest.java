package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class DefaultIdmCacheManagerIntegrationTest extends AbstractIntegrationTest {

    public static final String CACHE_NAME_1 = "TEST_CACHE1";
    public static final String CACHE_NAME_2 = "TEST_CACHE2";
    public static final String CACHE_NAME_3 = "TEST_CACHE3";
    public static final String CACHE_NAME_4 = "TEST_CACHE4";
    public static final String CACHE_NAME_5 = "TEST_CACHE5";

    @Autowired
    private DefaultIdmCacheManager cacheManager;

    @Autowired
    CacheManager springCacheManager;

    @Test
    public void testCacheInsert() {
        cacheManager.cacheValue(CACHE_NAME_1, "1", "val1");
        cacheManager.cacheValue(CACHE_NAME_1, "2", "val2");

        Assert.assertEquals("val1", cacheManager.getValue(CACHE_NAME_1, "1").get());
        Assert.assertEquals("val2", cacheManager.getValue(CACHE_NAME_1, "2").get());
        Assert.assertNull(cacheManager.getValue(CACHE_NAME_1, "3"));
    }

    @Test
    public void testCacheEvictSingleValue() {
        cacheManager.cacheValue(CACHE_NAME_2, "1", "val1");
        cacheManager.cacheValue(CACHE_NAME_2, "2", "val2");

        Assert.assertEquals("val1", cacheManager.getValue(CACHE_NAME_2, "1").get());
        Assert.assertEquals("val2", cacheManager.getValue(CACHE_NAME_2, "2").get());

        cacheManager.evictValue(CACHE_NAME_2, "1");

        Assert.assertNull(cacheManager.getValue(CACHE_NAME_2, "1"));
        Assert.assertNotNull(cacheManager.getValue(CACHE_NAME_2, "2"));
    }

    @Test
    public void testCacheEvictAllFromOneCache() {
        cacheManager.cacheValue(CACHE_NAME_3, "1", "val1");
        cacheManager.cacheValue(CACHE_NAME_3, "2", "val2");

        Assert.assertEquals("val1", cacheManager.getValue(CACHE_NAME_2, "1").get());
        Assert.assertEquals("val2", cacheManager.getValue(CACHE_NAME_2, "2").get());

        cacheManager.evictCache(CACHE_NAME_2);

        Assert.assertNull(cacheManager.getValue(CACHE_NAME_2, "1"));
        Assert.assertNull(cacheManager.getValue(CACHE_NAME_2, "2"));
    }

    @Test
    public void testCacheOverrideValue() {
        cacheManager.cacheValue(CACHE_NAME_3, "1", "val1");
        cacheManager.cacheValue(CACHE_NAME_3, "2", "val2");

        Assert.assertEquals("val1", cacheManager.getValue(CACHE_NAME_3, "1").get());
        Assert.assertEquals("val2", cacheManager.getValue(CACHE_NAME_3, "2").get());

        cacheManager.cacheValue(CACHE_NAME_3, "1", "val1changed");
        cacheManager.cacheValue(CACHE_NAME_3, "2", "val2changed");

        Assert.assertEquals("val1changed", cacheManager.getValue(CACHE_NAME_3, "1").get());
        Assert.assertEquals("val2changed", cacheManager.getValue(CACHE_NAME_3, "2").get());
    }

    @Test
    public void testCacheNullValue() {
        cacheManager.cacheValue(CACHE_NAME_4, "1", null);

        Assert.assertNotNull(cacheManager.getValue(CACHE_NAME_4, "1"));
        Assert.assertNull(cacheManager.getValue(CACHE_NAME_4, "1").get());
    }

    // TODO: do we need this? It would slow down retrieving entries from cache
    @Ignore
    @Test
    public void testCacheGetNotCreatesCache() {
        Cache.ValueWrapper val = cacheManager.getValue(CACHE_NAME_5, "key");

        Assert.assertNull(val);
        Assert.assertNull(cacheManager.getAllAvailableCaches().get()
                .filter(c -> CACHE_NAME_5.equals(c.getId())).findFirst().orElse(null));
    }

}


