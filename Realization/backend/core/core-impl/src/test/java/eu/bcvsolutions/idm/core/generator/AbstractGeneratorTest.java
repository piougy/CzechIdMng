package eu.bcvsolutions.idm.core.generator;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.GeneratorDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.service.IdmGeneratedValueService;
import eu.bcvsolutions.idm.core.api.service.ValueGeneratorManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Abstract class for all generators test
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public abstract class AbstractGeneratorTest extends AbstractIntegrationTest {

	@Autowired
	protected IdmGeneratedValueService generatedAttributeService;
	@Autowired
	protected ValueGeneratorManager valueGeneratorManager;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		cleanAllGenerator();
		super.logout();
	}
	
	/**
	 * Clean all created generators
	 */
	protected void cleanAllGenerator() {
		Page<IdmGeneratedValueDto> generators = generatedAttributeService.find(null);
		for (IdmGeneratedValueDto generator : generators) {
			generatedAttributeService.delete(generator);
		}
	}
	
	/**
	 * Create generated attribute
	 *
	 * @param entityType
	 * @param generatorType
	 * @param generatorProperties
	 * @param seq
	 * @param regenerateValue
	 * @return
	 */
	protected IdmGeneratedValueDto createGenerator(String entityType, String generatorType,
			ConfigurationMap generatorProperties, Integer seq, Boolean regenerateValue) {
		IdmGeneratedValueDto dto = new IdmGeneratedValueDto();
		dto.setEntityType(entityType);
		dto.setGeneratorType(generatorType);
		dto.setGeneratorProperties(generatorProperties);
		dto.setSeq(seq == null ? 0 : seq.shortValue());
		dto.setRegenerateValue(regenerateValue == null ? true : regenerateValue.booleanValue());
		return generatedAttributeService.save(dto);
	}

	/**
	 * Create configuration by given definition with values map
	 *
	 * @param formDefinition
	 * @param values
	 * @return
	 */
	protected ConfigurationMap createConfiguration(IdmFormDefinitionDto formDefinition, ImmutableMap<String, String> values) {
		ConfigurationMap generatorProperties = new ConfigurationMap();
		if (formDefinition != null && values != null) {
			for (IdmFormAttributeDto attr : formDefinition.getFormAttributes()) {
				String value = values.get(attr.getName());
				if (value != null) {
					generatorProperties.put(attr.getName(), values.get(attr.getName()));
				}
			}
		}
		return generatorProperties;
	}
	
	/**
	 * Create configuration for generator with given values
	 *
	 * @param values
	 * @return
	 */
	protected ConfigurationMap createConfiguration(ImmutableMap<String, String> values) {
		GeneratorDefinitionDto generator = getGenerator();
		IdmFormDefinitionDto formDefinition = generator.getFormDefinition();
		return this.createConfiguration(formDefinition, values);
	}

	/**
	 * Get generator from available generators with given entity type and generator type
	 * 
	 * @param entityType
	 * @param generatorType
	 * @return
	 */
	protected GeneratorDefinitionDto getGenerator(String entityType, String generatorType) {
		return this.valueGeneratorManager.getAvailableGenerators(entityType).stream().filter(gen -> gen.getGeneratorType().equals(generatorType)).findFirst().orElse(null);
	}

	/**
	 * Get generator for curent implementation given from method entity type and generator type
	 *
	 * @return
	 */
	protected GeneratorDefinitionDto getGenerator() {
		GeneratorDefinitionDto generator = this.getGenerator(getEntityType(), getGeneratorType());
		assertNotNull(generator);
		return generator;
	}

	/**
	 * Get entity type
	 *
	 * @return
	 */
	protected abstract String getEntityType();

	/**
	 * Get generator type
	 *
	 * @return
	 */
	protected abstract String getGeneratorType();
}
