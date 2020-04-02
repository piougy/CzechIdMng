package eu.bcvsolutions.idm.core.api.config.cache.domain;

import java.io.Serializable;

/**
 * Basic wrapper for all {@link Serializable} instances. It is used in caching in order to be able to cache null
 * values. This implementation of {@link ValueWrapper} is
 *
 * @param <K> Type of stored value
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public class SerializableCacheObjectWrapper<K extends Serializable> implements Serializable, ValueWrapper {

	private static final long serialVersionUID = 1L;

	private K val;

	// This constructor is needed for ehcache
	public SerializableCacheObjectWrapper() {

	}

	public SerializableCacheObjectWrapper(K val) {
		this.val = val;
	}

	@Override
	public K get() {
		return val;
	}

	public void setVal(K val) {
		this.val = val;
	}

	@Override
	public int hashCode() {
		if (this.val == null) {
			return this.getClass().getCanonicalName().hashCode() + Long.hashCode(serialVersionUID);
		}
		return this.getClass().getCanonicalName().hashCode() + this.val.hashCode() + Long.hashCode(serialVersionUID);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof SerializableCacheObjectWrapper) {
			@SuppressWarnings("unchecked")
			SerializableCacheObjectWrapper<Serializable> other = (SerializableCacheObjectWrapper<Serializable>) obj;
			return (val == null && other.get() == null) || (val != null && val.equals(other.get()));
		}
		return false;
	}

}
