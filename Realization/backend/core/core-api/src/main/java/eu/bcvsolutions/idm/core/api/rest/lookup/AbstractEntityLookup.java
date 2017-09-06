package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.core.GenericTypeResolver;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

/**
 * Lookup support
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 */
public abstract class AbstractEntityLookup<E extends BaseEntity> implements EntityLookup<E>, ScriptEnabled {

	private final Class<?> entityClass;

	/**
	 * Creates a new {@link AbstractEntityLookup} instance discovering the supported type from the generics signature.
	 */
	public AbstractEntityLookup() {
		this.entityClass = GenericTypeResolver.resolveTypeArgument(getClass(), EntityLookup.class);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return entityClass.isAssignableFrom(delimiter);
	}
	
	/**
	 * Returns entity class for this lookup
	 * 
	 * @return
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}
}