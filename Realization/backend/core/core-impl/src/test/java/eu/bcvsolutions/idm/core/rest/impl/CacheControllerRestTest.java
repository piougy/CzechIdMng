package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmCacheManager} and {@link CacheController}
 * 
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 *
 */
public class CacheControllerRestTest extends AbstractRestTest{

	public static final String TEST_CACHE_NAME = "TEST_CACHE";
	public static final String TEST_CACHE_NAME_2 = "TEST_CACHE_2";
	public static final String TEST_CACHE_NAME_3 = "TEST_CACHE_3";
	public static final String TEST_CACHE_NAME_4 = "TEST_CACHE_4";
	public static final String TEST_CACHE_NAME_5 = "TEST_CACHE_5";

	@Autowired
	CacheManager cacheManager;

	@Test
	public void testFindAll() {
		Cache cache = cacheManager.getCache(TEST_CACHE_NAME);
		cache.put("key1", "val1");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> TEST_CACHE_NAME.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(1, testCache.getSize());
	}

	@Test
	public void testEvict() {
		Cache cache = cacheManager.getCache(TEST_CACHE_NAME_2);
		cache.put("key1", "val1");
		cache.put("key2", "val2");
		cache.put("key3", "val3");
		cache.put("key4", "val4");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> TEST_CACHE_NAME_2.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(4, testCache.getSize());

		int resultCode = evict(TEST_CACHE_NAME_2);
		Assert.assertEquals(204, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> TEST_CACHE_NAME_2.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertEquals(0, testCacheAfterEvict.getSize());
	}

	@Test
	public void testEvictNotExisting() {
		Cache cache = cacheManager.getCache(TEST_CACHE_NAME_3);
		cache.put("key1", "val1");
		cache.put("key2", "val2");
		cache.put("key3", "val3");
		cache.put("key4", "val4");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> TEST_CACHE_NAME_3.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(4, testCache.getSize());

		final String notExistingCacheName = TEST_CACHE_NAME_3 + UUID.randomUUID();
		final int resultCode = evict(notExistingCacheName);
		Assert.assertEquals(404, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> TEST_CACHE_NAME_3.equals(c.getName())).findFirst().orElse(null);
		IdmCacheDto notExistingCache = resultsAfterEvict.stream().filter(c -> notExistingCacheName.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNull(notExistingCache);
		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertEquals(4, testCacheAfterEvict.getSize());
	}

	@Test
	public void testEvictAll() {
		Cache cache = cacheManager.getCache(TEST_CACHE_NAME_4);
		cache.put("key1", "val1");
		cache.put("key2", "val2");
		cache.put("key3", "val3");
		cache.put("key4", "val4");
		Cache cache2 = cacheManager.getCache(TEST_CACHE_NAME_5);
		cache2.put("key5", "val5");
		cache2.put("key6", "val6");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> TEST_CACHE_NAME_4.equals(c.getName())).findFirst().orElse(null);
		IdmCacheDto testCache2 = results.stream().filter(c -> TEST_CACHE_NAME_5.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertNotNull(testCache2);
		Assert.assertEquals(4, testCache.getSize());
		Assert.assertEquals(2, testCache2.getSize());

		final int resultCode = evictAll();
		Assert.assertEquals(204, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> TEST_CACHE_NAME_4.equals(c.getName())).findFirst().orElse(null);
		IdmCacheDto testCache2AfterEvict = resultsAfterEvict.stream().filter(c -> TEST_CACHE_NAME_5.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertNotNull(testCache2AfterEvict);
		Assert.assertEquals(0, testCacheAfterEvict.getSize());
		Assert.assertEquals(0, testCache2AfterEvict.getSize());
	}

	private int evictAll() {
		try {
			return getMockMvc().perform(put(BaseController.BASE_PATH + "/caches/evict")
					.with(authentication(getAdminAuthentication()))
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andReturn().getResponse().getStatus();
			//
		} catch (Exception ex) {
			throw new RuntimeException("Failed evict all", ex);
		}
	}

	private int evict(final String name) {
		try {
			return getMockMvc().perform(put(BaseController.BASE_PATH + "/caches/" + name + "/evict")
					.with(authentication(getAdminAuthentication()))
					.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andReturn().getResponse().getStatus();
			//
		} catch (Exception ex) {
			throw new RuntimeException("Failed evict cache " + name, ex);
		}
	}

	protected List<IdmCacheDto> find() {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/caches")
	        		.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, IdmCacheDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
