package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Generatable;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.GeneratorDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.generator.ValueGenerator;
import eu.bcvsolutions.idm.core.api.service.IdmGeneratedValueService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;

/**
 * Default implementation manager for generating value
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("valueGeneratorManager")
public class DefaultValueGeneratorManager implements ValueGeneratorManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultValueGeneratorManager.class);

	@Autowired
	private IdmGeneratedValueService service;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private LookupService lookupService;

	// generators are cached
	private final Map<String, ValueGenerator<? extends AbstractDto>> generators = new HashMap<>();
	// supported entities are cached
	private final Set<String> supportedEntities = new HashSet<>();
	
	@Override
	public <E extends AbstractDto> E generate(E dto) {
		Assert.notNull(dto);
		//
		List<IdmGeneratedValueDto> valueGenerators = service.getEnabledGenerator(getEntityClass(dto));

		// iterate over saved and enabled generators in DB for given entity type
		for (IdmGeneratedValueDto valueGenerator : valueGenerators) {
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
	public List<GeneratorDefinitionDto> getAvailableGenerators(String entityType) {
		// TODO: use cache
		List<GeneratorDefinitionDto> result = new ArrayList<>();
		Set<Entry<String, ValueGenerator>> allGenerators = context.getBeansOfType(ValueGenerator.class).entrySet();
		
		if (entityType != null) {
			try {
				Class<?> entityClass = Class.forName(entityType);
				for (Entry<String, ValueGenerator> entry : allGenerators) {
					ValueGenerator<?> generator = entry.getValue();
					if (!generator.isDisabled() && generator.supports(entityClass)) {
						result.add(valueGeneratorToDto(generator));
					}
				}
			} catch (ClassNotFoundException e) {
				LOG.error("Class [{}] not found.", entityType, e);
				throw new ResultCodeException(CoreResultCode.GENERATOR_ENTITY_CLASS_NOT_FOUND, ImmutableMap.of("class", entityType));
			}

		} else {
			// class name is null transform all
			allGenerators.stream().map(Entry::getValue).forEach(generator -> result.add(valueGeneratorToDto(generator)));
		}

		return result;
	}
	
	@Override
	public Set<String> getSupportedEntityTypes() {
		if (!supportedEntities.isEmpty()) {
			return supportedEntities;
		}
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		for (EntityType<?> entity : entities) {
			if (entity.getJavaType() == null) {
				continue;
			}
			if (Generatable.class.isAssignableFrom(entity.getJavaType())) {
				supportedEntities.add(entity.getJavaType().getCanonicalName());
			}
		}

		return supportedEntities;
	}

	@Override
	public boolean supportsGenerating(Identifiable type) {
		Class<? extends BaseEntity> ownerEntityType = getEntityClass(type);
		return getSupportedEntityTypes().contains(ownerEntityType.getCanonicalName());
	}

	/**
	 * Return entity class for {@link Identifiable}
	 *
	 * @param identifiable
	 * @return
	 */
	private Class<? extends BaseEntity> getEntityClass(Identifiable identifiable) {
		return lookupService.getEntityClass(identifiable.getClass());
	}
	
	/**
	 * Transform {@link ValueGenerator} to {@link GeneratorDefinitionDto}.
	 * {@link GeneratorDefinitionDto} was prepared for send to fronted agenda.
	 *
	 * @param valueGenerator
	 * @return
	 */
	private GeneratorDefinitionDto valueGeneratorToDto(ValueGenerator<?> valueGenerator) {
		GeneratorDefinitionDto valueGeneratorDto = new GeneratorDefinitionDto();
		valueGeneratorDto.setDescription(valueGenerator.getDescription());
		valueGeneratorDto.setModule(valueGenerator.getModule());
		valueGeneratorDto.setEntityType(valueGenerator.getEntityClass().getCanonicalName());
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
	private <E extends AbstractDto> ValueGenerator<E> getGenerator(IdmGeneratedValueDto generatorDto) {
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
		if (generator.isDisabled()) {
			// generator can be disabled in runtime - disable module
			LOG.info("Generator type [{}] is disabled.", generatorType);
			return null;
		}

		return generator;
	}
}
