package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.LocalIdMCacheConfiguration;


/**
 * Caches used in tests.
 * 
 * @author Peter Štrunc
 */
@Profile("test")
@Configuration
public class TestCacheConfiguration {

	@Bean
	public IdMCacheConfiguration test1CacheConfig() {
		return LocalIdMCacheConfiguration.<String, String> builder()
				.withName(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_1)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

	@Bean
	public IdMCacheConfiguration test2CacheConfig() {
		return LocalIdMCacheConfiguration.<String, String> builder()
				.withName(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_2)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

	@Bean
	public IdMCacheConfiguration test3CacheConfig() {
		return LocalIdMCacheConfiguration.<String, String> builder()
				.withName(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_3)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

	@Bean
	public IdMCacheConfiguration test4CacheConfig() {
		return LocalIdMCacheConfiguration.<String, String> builder()
				.withName(DefaultIdmCacheManagerIntegrationTest.CACHE_NAME_4)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

}
