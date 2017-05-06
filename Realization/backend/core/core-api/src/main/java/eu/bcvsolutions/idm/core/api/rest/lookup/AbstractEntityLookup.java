package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.core.GenericTypeResolver;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Lookup support
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractEntityLookup<E extends BaseEntity> implements EntityLookup<E> {

	private final Class<?> domainType;

	/**
	 * Creates a new {@link AbstractEntityLookup} instance discovering the supported type from the generics signature.
	 */
	public AbstractEntityLookup() {
		this.domainType = GenericTypeResolver.resolveTypeArgument(getClass(), EntityLookup.class);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return domainType.isAssignableFrom(delimiter);
	}
}