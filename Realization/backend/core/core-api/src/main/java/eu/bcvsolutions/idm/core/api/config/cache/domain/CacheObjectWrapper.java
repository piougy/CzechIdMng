package eu.bcvsolutions.idm.core.api.config.cache.domain;

/**
 * Basic wrapper for all {@link Object} instances. It is used in caching in order to be able to cache null
 * values.
 *
 * @param <K> Type of stored value
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
public class CacheObjectWrapper<K> implements ValueWrapper {

	private static final long serialVersionUID = 1L;

	private K val;

	// This constructor is needed for ehcache
	public CacheObjectWrapper() {

	}

	public CacheObjectWrapper(K val) {
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
		if (obj instanceof CacheObjectWrapper) {
			CacheObjectWrapper<?> other = (CacheObjectWrapper<?>) obj;
			return (val == null && other.get() == null) || (val != null && val.equals(other.get()));
		}
		return false;
	}
}
