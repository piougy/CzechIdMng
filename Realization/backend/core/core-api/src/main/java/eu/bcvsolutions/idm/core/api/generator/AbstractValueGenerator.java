package eu.bcvsolutions.idm.core.api.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;

/**
 * Abstract class for value generators
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <E> generator is designed for only one entity type
 */
public abstract class AbstractValueGenerator<E extends AbstractDto> implements ValueGenerator<E>{

	@Autowired
	private ModuleService moduleService;
	@Autowired
	private ConfigurationService configurationService;

	/**
	 * Method generate values for given DTO. The method must implements all
	 * child classes.
	 *
	 * @param dto
	 * @return
	 */
	protected abstract E generateItem(E dto, IdmGeneratedValueDto valueGenerator);

	@Override
	public E generate(E dto, IdmGeneratedValueDto valueGenerator) {
		Assert.notNull(dto);
		//
		if (valueGenerator == null) {
			// generated attribute must be set before generate
			throw new ResultCodeException(CoreResultCode.GENERATOR_GENERATED_ATTRIBUTES_IS_NULL, ImmutableMap.of("generator", this.getName()));
		}
		//
		return generateItem(dto, valueGenerator);
	}

	@Override
	public boolean isDisabled() {
		// value generators can't be disabled by property, only by module
		return !moduleService.isEnabled(this.getModule());
	}

	@Override
	public boolean supports(Class<?> entityType) {
		Assert.notNull(entityType);
		//
		return this.getEntityClass().isAssignableFrom(entityType);
	}

	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
