package eu.bcvsolutions.idm.core.api.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Abstract class for value generators
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 * @param <E> generator is designed for only one dto type
 */
public abstract class AbstractValueGenerator<E extends AbstractDto> implements ValueGenerator<E> {

	private final Class<E> dtoClass;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@SuppressWarnings({ "unchecked" })
	public AbstractValueGenerator() {
		this.dtoClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), ValueGenerator.class);
	}

	@Override
	public Class<E> getDtoClass() {
		return dtoClass;
	}

	/**
	 * Method generate values for given DTO. The method must implements all
	 * child classes.
	 *
	 * @param dto
	 * @param generatorConfiguration
	 * @return
	 */
	protected abstract E generateItem(E dto, IdmGenerateValueDto generatorConfiguration);

	@Override
	public E generate(E dto, IdmGenerateValueDto generatorConfiguration) {
		Assert.notNull(dto);
		//
		if (generatorConfiguration == null) {
			// generated attribute must be set before generate
			throw new ResultCodeException(CoreResultCode.GENERATOR_GENERATED_ATTRIBUTES_IS_NULL, ImmutableMap.of("generator", this.getName()));
		}
		//
		return generateItem(dto, generatorConfiguration);
	}

	@Override
	public boolean supports(Class<?> dtoType) {
		Assert.notNull(dtoType);
		//
		return this.getDtoClass().isAssignableFrom(dtoType);
	}

	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
