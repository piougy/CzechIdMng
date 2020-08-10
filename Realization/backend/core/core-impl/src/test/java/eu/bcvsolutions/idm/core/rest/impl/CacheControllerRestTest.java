package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmCacheManagerIntegrationTest;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Tests for {@link eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmCacheManager} and {@link CacheController}
 * 
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
public class CacheControllerRestTest extends AbstractRestTest {

	@Autowired private IdmCacheManager cacheManager;
	
	@Before
    public void setup() {
    	cacheManager.evictAllCaches();
    }

	@Test
	public void testFindAll() {
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_1,"key1", "val1");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_1.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
	}

	@Test
	public void testEvict() {
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2, "key1", "val1");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2, "key2", "val2");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2, "key3", "val3");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2, "key4", "val4");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(4, testCache.getSize());

		int resultCode = evict(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2);
		Assert.assertEquals(204, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertEquals(0, testCacheAfterEvict.getSize());
	}

	/**
	 * Not exist cache will be not registered in cache manager as empty cache
	 */
	@Test
	public void testEvictNotExisting() {
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3, "key1", "val1");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3, "key2", "val2");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3, "key3", "val3");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3, "key4", "val4");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(4, testCache.getSize());

		final String notExistingCacheName = DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3 + UUID.randomUUID();
		final int resultCode = evict(notExistingCacheName);
		Assert.assertEquals(204, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3.equals(c.getName())).findFirst().orElse(null);
		IdmCacheDto notExistingCache = resultsAfterEvict.stream().filter(c -> notExistingCacheName.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNull(notExistingCache);
		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertEquals(4, testCacheAfterEvict.getSize());
	}

	@Test
	public void testEvictAll() {
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4,"key1", "val1");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4,"key2", "val2");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4,"key3", "val3");
		cacheManager.cacheValue(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4,"key4", "val4");
		//
		List<IdmCacheDto> results = find();

		IdmCacheDto testCache = results.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCache);
		Assert.assertEquals(4, testCache.getSize());

		final int resultCode = evictAll();
		Assert.assertEquals(204, resultCode);

		List<IdmCacheDto> resultsAfterEvict = find();

		IdmCacheDto testCacheAfterEvict = resultsAfterEvict.stream().filter(c -> DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4.equals(c.getName())).findFirst().orElse(null);

		Assert.assertNotNull(testCacheAfterEvict);
		Assert.assertEquals(0, testCacheAfterEvict.getSize());
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
