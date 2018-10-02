package eu.bcvsolutions.idm.core.api.generator;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Interface for all generators
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 * @param <E>
 */
public interface ValueGenerator<E extends AbstractDto> extends Configurable {

	@Override
	default String getConfigurableType() {
		return "value-generator";
	}

	/**
	 * Returns dto class, which supports this generator
	 * 
	 * @return
	 */
	Class<E> getDtoClass();

	/**
	 * Generate values by given value generator configuration.
	 *
	 * @param dto
	 * @param generatorConfiguration 
	 * @return
	 */
	E generate(E dto, IdmGenerateValueDto generatorConfiguration);
	
	/**
	 * Returns true, when generator supports given entity type
	 * 
	 * @param dtoType
	 * @return
	 */
	boolean supports(Class<?> dtoType);
}
