package eu.bcvsolutions.idm.core.config.cache;

import org.ehcache.impl.serialization.PlainJavaSerializer;

import eu.bcvsolutions.idm.core.api.config.cache.domain.SerializableCacheObjectWrapper;

/**
 * Serializer for EhCache.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@SuppressWarnings("rawtypes")
public class SerializableCacheWrapperSerializer extends PlainJavaSerializer<SerializableCacheObjectWrapper>
		implements org.ehcache.spi.serialization.Serializer<SerializableCacheObjectWrapper> {

	public SerializableCacheWrapperSerializer(ClassLoader classLoader) {
		super(classLoader);
	}

}