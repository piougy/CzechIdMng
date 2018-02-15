package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Read {@link BaseDto} by uuid identifier or by {@link Codeable} identifier.
 * 
 * @author Radek Tomiška
 *
 * @param <T>
 */
public interface DtoLookup<T extends BaseDto> extends Plugin<Class<?>> {

	/**
	 * Gets {@link BaseDto} identifier - {@link Codeable} identifier has higher priority.
	 * 
	 * @param dto
	 * @return
	 */
	Serializable getIdentifier(T dto);

	/**
	 * Returns {@link BaseDto} by given identifier.
	 * 
	 * @param id
	 * @return {@link BaseDto}
	 */
	T lookup(Serializable id);
}
