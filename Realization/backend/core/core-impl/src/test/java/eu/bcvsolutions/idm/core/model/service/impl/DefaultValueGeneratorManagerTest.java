package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.Generatable;
import eu.bcvsolutions.idm.core.api.dto.GeneratorDefinitionDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.generator.AbstractGeneratorTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Integration tests for default implementation of generator manager
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultValueGeneratorManagerTest extends AbstractGeneratorTest {

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		cleanAllGenerator();
		super.logout();
	}

	@Test
	public void testCreateGenerator() {
		Set<String> supportedEntityTypes = valueGeneratorManager.getSupportedEntityTypes();
		//
		supportedEntityTypes.forEach(supportedType -> {
			List<GeneratorDefinitionDto> availableGenerators = valueGeneratorManager
					.getAvailableGenerators(supportedType);
			availableGenerators.forEach(generator -> {
				IdmFormDefinitionDto formDefinition = generator.getFormDefinition();
				//
				createGenerator(supportedType, generator.getGeneratorType(),
						createConfiguration(formDefinition, ImmutableMap.of()), null, null);
			});
		});
	}

	@Test
	public void checkSupportedEntityTypes() {
		Set<String> supportedEntityTypes = valueGeneratorManager.getSupportedEntityTypes();
		for (String type : supportedEntityTypes) {
			try {
				Class<?> forName = Class.forName(type);
				assertTrue(AbstractEntity.class.isAssignableFrom(forName));
				assertTrue(Generatable.class.isAssignableFrom(forName));
			} catch (ClassNotFoundException e) {
				fail(e.getMessage());
			}
		}
	}

	@Test
	public void testGeneratorTypes() {
		List<GeneratorDefinitionDto> generators = valueGeneratorManager.getAvailableGenerators(null);
		
		for (GeneratorDefinitionDto generator : generators) {
			try {
				Class.forName(generator.getEntityType());
				Class.forName(generator.getGeneratorType());
				} catch (ClassNotFoundException e) {
				fail(e.getMessage());
			}
		}
	}

	@Test
	public void testGeneratorTypesForIdentity() {
		List<GeneratorDefinitionDto> generators = valueGeneratorManager
				.getAvailableGenerators(IdmIdentity.class.getCanonicalName());

		for (GeneratorDefinitionDto generator : generators) {
			try {
				Class.forName(generator.getEntityType());
				Class.forName(generator.getGeneratorType());
				assertEquals(IdmIdentity.class.getCanonicalName(), generator.getEntityType());
			} catch (ClassNotFoundException e) {
				fail(e.getMessage());
			}
		}
	}

	@Override
	protected String getEntityType() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getGeneratorType() {
		throw new UnsupportedOperationException();
	}
}
