package eu.bcvsolutions.idm.core.model.repository.listener;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.Auditable;

/**
 * Prevent created and creator field from update
 * 
 * @see {@link Auditable}
 * @author Radek Tomiška
 *
 */
public class AuditableInterceptor extends EmptyInterceptor {
	
	private static final long serialVersionUID = -6448069539808650939L;
	private static final Set<String> unmodifiablePropertyNames = Sets.newHashSet(
			Auditable.PROPERTY_CREATED,
			Auditable.PROPERTY_CREATOR,
			Auditable.PROPERTY_CREATOR_ID,
			Auditable.PROPERTY_ORIGINAL_CREATOR,
			Auditable.PROPERTY_ORIGINAL_CREATOR_ID
			);

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		//
		if (!(entity instanceof Auditable)) {
			return false;
		}
		//
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (unmodifiablePropertyNames.contains(propertyName)) {
				if (previousState[i] == null) {
					// new value
					continue;
				}
				if (!previousState[i].equals(currentState[i])) {
					currentState[i] = previousState[i];
					return true;
				}
			}
		}
		return false;
	}
}
