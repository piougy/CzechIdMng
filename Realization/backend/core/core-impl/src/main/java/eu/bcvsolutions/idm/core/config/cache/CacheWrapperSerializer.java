package eu.bcvsolutions.idm.core.config.cache;

import org.ehcache.impl.serialization.PlainJavaSerializer;
import org.ehcache.spi.serialization.Serializer;

import eu.bcvsolutions.idm.core.api.config.cache.domain.CacheObjectWrapper;

/**
 * Serializer for EhCache.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@SuppressWarnings("rawtypes")
public class CacheWrapperSerializer extends PlainJavaSerializer<CacheObjectWrapper> implements Serializer<CacheObjectWrapper> {

	public CacheWrapperSerializer(ClassLoader classLoader) {
		super(classLoader);
	}

}
