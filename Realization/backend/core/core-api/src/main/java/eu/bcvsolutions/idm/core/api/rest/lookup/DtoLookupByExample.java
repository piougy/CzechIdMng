package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Find {@link BaseDto} by {@link BaseDto} example ~ fill, what you know into example.
 * 
 * @author Radek Tomiška
 * @param <T> dto type
 * @since 10.8.0
 */
public interface DtoLookupByExample<T extends BaseDto> extends Plugin<Class<?>> {

	/**
	 * Returns {@link BaseDto} by given example.
	 * 
	 * @param example ~ fill, what you know into example.
	 * @return {@link BaseDto}
	 */
	T lookup(T example);
}
