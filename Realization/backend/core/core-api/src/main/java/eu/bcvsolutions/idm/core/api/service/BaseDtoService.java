package eu.bcvsolutions.idm.core.api.service;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * All DTO services using this interface.
 * 
 * @author Svanda
 *
 * @param <T> {@link BaseDto} type
 */
public interface BaseDtoService<DTO extends BaseDto> extends Plugin<Class<?>> {

	/**
	 * Returns {@link BaseDto} type class, which is controlled by this service
	 * 
	 * @return
	 */
	public Class<DTO> getDtoClass();
}
