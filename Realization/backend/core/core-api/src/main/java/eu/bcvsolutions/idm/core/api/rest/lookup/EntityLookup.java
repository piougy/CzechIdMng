package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

public interface EntityLookup<T extends BaseEntity> extends Plugin<Class<?>> {

	Serializable getIdentifier(T entity);

	T lookup(Serializable id);
}
