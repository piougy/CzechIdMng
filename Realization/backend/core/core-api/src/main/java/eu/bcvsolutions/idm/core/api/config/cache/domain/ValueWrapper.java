package eu.bcvsolutions.idm.core.api.config.cache.domain;

/**
 * Interface for null value wrappers used in {@link eu.bcvsolutions.idm.core.api.service.IdmCacheManager}
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public interface ValueWrapper {

	/**
	 * @return Stored value. Note that it may be null.
	 */
	Object get();
}
