package eu.bcvsolutions.idm.core.api.generator;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Interface for all generators
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <E>
 */
public interface ValueGenerator<E extends AbstractDto> extends Configurable {

	@Override
	default String getConfigurableType() {
		return "value-generator";
	}

	/**
	 * Returns entity class, which supports this processor
	 * 
	 * @return
	 */
	Class<? extends AbstractEntity> getEntityClass();

	/**
	 * Generate values
	 *
	 * @param dto
	 * @param valueGenerator 
	 * @return
	 */
	E generate(E dto,  IdmGeneratedValueDto valueGenerator);
	
	/**
	 * Returns true, when generator supports given entity type
	 * 
	 * @param authorizableType
	 * @return
	 */
	boolean supports(Class<?> entityType);
}
