package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

public interface DtoLookup<T extends BaseDto> extends Plugin<Class<?>> {

	Serializable getIdentifier(T entity);

	T lookup(Serializable id);
}
