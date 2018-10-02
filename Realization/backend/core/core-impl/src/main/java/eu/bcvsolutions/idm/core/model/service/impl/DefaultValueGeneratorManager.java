package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Shorts;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.ValueGeneratorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmGenerateValueFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.generator.ValueGenerator;
import eu.bcvsolutions.idm.core.api.service.IdmGenerateValueService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Default implementation manager for generating value
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service("valueGeneratorManager")
public class DefaultValueGeneratorManager implements ValueGeneratorManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultValueGeneratorManager.class);

	@Autowired
	private IdmGenerateValueService service;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private EnabledEvaluator enabledEvaluator;

	// generators are cached
	private final Map<String, ValueGenerator<? extends AbstractDto>> generators = new HashMap<>();
	// supported entities are cached
	private Set<Class< ? extends AbstractDto>> supportedTypes = null;
	
	@Override
	public <E extends AbstractDto> E generate(E dto) {
		Assert.notNull(dto);
		//
		List<IdmGenerateValueDto> valueGenerators = getEnabledGenerators(dto.getClass());

		// iterate over saved and enabled generators in DB for given entity type
		for (IdmGenerateValueDto valueGenerator : valueGenerators) {
			ValueGenerator<E> generator = getGenerator(valueGenerator);
			if (generator != null) {
				//
				dto = generator.generate(dto, valueGenerator);
				if (dto == null) {
					// generator return null, its broken
					LOG.error("Generator [{}] return null!", generator.getName());
					throw new ResultCodeException(CoreResultCode.GENERATOR_RETURN_NULL, ImmutableMap.of("generator", generator.getName()));
				}
			}
		}

		return dto;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public List<ValueGeneratorDto> getAvailableGenerators(Class<? extends AbstractDto> dtoType) {
		// TODO: use cache
		List<ValueGeneratorDto> result = new ArrayList<>();
		Set<Entry<String, ValueGenerator>> allGenerators = context.getBeansOfType(ValueGenerator.class).entrySet();
		
		if (dtoType != null) {
			for (Entry<String, ValueGenerator> entry : allGenerators) {
				ValueGenerator<?> generator = entry.getValue();
				if (!generator.isDisabled() 
						&& enabledEvaluator.isEnabled(generator)
						&& generator.supports(dtoType)) {
					result.add(valueGeneratorToDto(generator));
				}
			}
		} else {
			// class name is null transform all
			allGenerators
				.stream()
				.map(Entry::getValue)
				.forEach(generator -> result.add(valueGeneratorToDto(generator)));
		}

		return result;
	}
	
	@Override
	public Set<Class< ? extends AbstractDto>> getSupportedTypes() {
		if (supportedTypes == null) {
			supportedTypes = context.getBeansOfType(ValueGenerator.class)
					.values()
					.stream()
					.map(ValueGenerator::getDtoClass)
					.collect(Collectors.toSet());
			
			
		}
		//
		return supportedTypes;
	}

	@Override
	public boolean supportsGenerating(AbstractDto dto) {
		Assert.notNull(dto);
		//
		return getSupportedTypes().contains(dto.getClass());
	}
	
	/**
	 * Transform {@link ValueGenerator} to {@link ValueGeneratorDto}.
	 * {@link ValueGeneratorDto} was prepared for send to fronted agenda.
	 *
	 * @param valueGenerator
	 * @return
	 */
	private ValueGeneratorDto valueGeneratorToDto(ValueGenerator<?> valueGenerator) {
		ValueGeneratorDto valueGeneratorDto = new ValueGeneratorDto();
		valueGeneratorDto.setDescription(valueGenerator.getDescription());
		valueGeneratorDto.setModule(valueGenerator.getModule());
		valueGeneratorDto.setDtoType(valueGenerator.getDtoClass().getCanonicalName());
		valueGeneratorDto.setDisabled(valueGenerator.isDisabled());
		valueGeneratorDto.setName(valueGenerator.getName());
		valueGeneratorDto.setGeneratorType(valueGenerator.getClass().getCanonicalName());
		valueGeneratorDto.setFormDefinition(valueGenerator.getFormDefinition());
		return valueGeneratorDto;
	}
	/**
	 * Return generator for given DTO
	 *
	 * @param generatorDto
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <E extends AbstractDto> ValueGenerator<E> getGenerator(IdmGenerateValueDto generatorDto) {
		String generatorType = generatorDto.getGeneratorType();
		if (!generators.containsKey(generatorType)) {
			// generator doesn't exist in cache, load it
			try {
				ValueGenerator<E> bean = (ValueGenerator<E>) context.getBean(Class.forName(generatorType));
				generators.put(generatorType, bean);
			} catch (BeansException | ClassNotFoundException e) {
				// probably disabled or removed generators
				LOG.warn("Generator for type [{}] doesn't exist. Or more generators found for type.", generatorType);
				return null;
			}
		}

		ValueGenerator<E> generator = (ValueGenerator<E>) generators.get(generatorType);
		if (generator.isDisabled() || !enabledEvaluator.isEnabled(generator)) {
			// generator can be disabled in runtime - disable module
			LOG.info("Generator type [{}] is disabled.", generatorType);
			return null;
		}

		return generator;
	}
	
	private List<IdmGenerateValueDto> getEnabledGenerators(Class<? extends AbstractDto> dtoType) {
		Assert.notNull(dtoType);
		//
		IdmGenerateValueFilter filter = new IdmGenerateValueFilter();
		filter.setDisabled(false);
		filter.setDtoType(dtoType.getCanonicalName());

		// we must create new instance of arraylist, given list is unmodifable
		List<IdmGenerateValueDto> generators = new ArrayList<>(service.find(filter, null).getContent());

		// sort by order
		Collections.sort(generators, new Comparator<IdmGenerateValueDto>() {
			@Override
			public int compare(IdmGenerateValueDto o1, IdmGenerateValueDto o2) {
				return Shorts.compare(o1.getSeq(), o2.getSeq());
			}
		});
		return generators;
	}
}
