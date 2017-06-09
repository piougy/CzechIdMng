package eu.bcvsolutions.idm.core.api.service;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * All DTO services using this interface.
 * 
 * @param <DTO> {@link BaseDto} type
 * @author Svanda
 * @author Radek Tomiška
 */
public interface BaseDtoService<DTO extends BaseDto> extends Plugin<Class<?>> {

	/**
	 * Returns {@link BaseDto} type class, which is controlled by this service
	 * 
	 * @return
	 */
	public Class<DTO> getDtoClass();
	
	/**
	 * Returns {@link BaseEntity} type class, which is controlled by this service
	 * 
	 * @return
	 */
	public Class<? extends BaseEntity> getEntityClass();
}
