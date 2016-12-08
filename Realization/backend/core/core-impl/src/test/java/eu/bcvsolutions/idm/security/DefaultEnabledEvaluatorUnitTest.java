package eu.bcvsolutions.idm.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.domain.Enabled;
import eu.bcvsolutions.idm.security.api.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.security.api.exception.ModuleDisabledException;
import eu.bcvsolutions.idm.security.service.impl.DefaultEnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for {@link DefaultEnabledEvaluator}
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEnabledEvaluatorUnitTest extends AbstractUnitTest {

	private static final String MODULE_ONE = "test-module-one";
	private static final String MODULE_TWO = "test-module-two";
	private static final String PROPERTY_ONE = "test-property-one";
	private static final String PROPERTY_TWO = "test-property-two";

	@Mock
	private IdmConfigurationService configurationService;

	@Mock
	private ModuleService moduleService;

	private DefaultEnabledEvaluator evaluator;

	@Before
	public void init() {
		evaluator = new DefaultEnabledEvaluator(moduleService, configurationService);
	}

	/**
	 * Constructs test {@link Enabled} annotation.
	 * 
	 * @param modules
	 * @param properties
	 * @param asValue if given modules will be set to value or module annotation property.
	 * @return
	 */
	private Enabled prepareAnnotation(String[] modules, String[] properties, boolean asValue) {
		return new Enabled() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Enabled.class;
			}

			@Override
			public String[] value() {
				return (asValue && modules != null) ? modules : new String[]{};
			}

			@Override
			public String[] property() {
				return properties != null ? properties : new String[]{};
			}

			@Override
			public String[] module() {
				return (!asValue && modules != null) ? modules : new String[]{};
			}
		};
	}

	/**
	 * Empty annotation will be ignored
	 */
	@Test
	public void testEmptyAsTrue() {
		// no exception occurs
		Enabled enabled = prepareAnnotation(null, null, false);
		evaluator.checkEnabled(enabled);
		assertTrue(evaluator.isEnabled(enabled));
	}

	@Test
	public void testEnabledModuleOne() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		try {
			evaluator.checkEnabled(prepareAnnotation(new String[]{ MODULE_ONE}, null, false));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testDisabledModuleOne() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		try {
			evaluator.checkEnabled(prepareAnnotation(new String[]{ MODULE_ONE}, null, false));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}
	
	@Test
	public void testEnabledModuleOneAsValue() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		
		evaluator.checkEnabled(prepareAnnotation(new String[]{ MODULE_ONE}, null, true));
		
		verify(moduleService).isEnabled(MODULE_ONE);
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testDisabledModuleOneAsValue() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		try {
			evaluator.checkEnabled(prepareAnnotation(new String[]{ MODULE_ONE}, null, true));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}	
	
	@Test
	public void testEnabledPropertyOne() {
		when(configurationService.getBooleanValue(PROPERTY_ONE, false)).thenReturn(true);
		
		evaluator.checkEnabled(prepareAnnotation(null, new String[]{ PROPERTY_ONE}, true));
		
		verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
	}
	
	
	@Test(expected = ConfigurationDisabledException.class)
	public void testDisabledPropertyOne() {
		when(configurationService.getBooleanValue(PROPERTY_ONE, false)).thenReturn(false);
		
		try {
			evaluator.checkEnabled(prepareAnnotation(null, new String[]{ PROPERTY_ONE}, true));
		} finally {
			verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
		}
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testAnyDisabledModule() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		when(moduleService.isEnabled(MODULE_TWO)).thenReturn(false);
		try {
			evaluator.checkEnabled(prepareAnnotation(new String[]{ MODULE_ONE, MODULE_TWO}, null, true));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
			verify(moduleService).isEnabled(MODULE_TWO);
		}
	}
	
	@Test(expected = ConfigurationDisabledException.class)
	public void testAnyDisabledProperty() {
		when(configurationService.getBooleanValue(PROPERTY_ONE, false)).thenReturn(true);
		when(configurationService.getBooleanValue(PROPERTY_TWO, false)).thenReturn(false);
		try {
			evaluator.checkEnabled(prepareAnnotation(null, new String[]{ PROPERTY_ONE, PROPERTY_TWO}, true));
		} finally {
			verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
			verify(configurationService).getBooleanValue(PROPERTY_TWO, false);
		}
	}
	
	@Test
	public void testClassEnabled() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		//
		@Enabled(MODULE_ONE)
		class EnabledObject {}
		assertTrue(evaluator.isEnabled(EnabledObject.class));
		//
		verify(moduleService).isEnabled(MODULE_ONE);
	}
	
	@Test
	public void testClassDisabled() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		//
		@Enabled(MODULE_ONE)
		class EnabledObject {}
		assertFalse(evaluator.isEnabled(EnabledObject.class));
		//
		verify(moduleService).isEnabled(MODULE_ONE);
	}
	
	@Test
	public void testObjectEnabled() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		//
		@Enabled(MODULE_ONE)
		class EnabledObject {}
		assertTrue(evaluator.isEnabled(new EnabledObject()));
		//
		verify(moduleService).isEnabled(MODULE_ONE);
	}
	
	@Test
	public void testObjectDisabled() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		//
		@Enabled(MODULE_ONE)
		class EnabledObject {}
		assertFalse(evaluator.isEnabled(new EnabledObject()));
		//
		verify(moduleService).isEnabled(MODULE_ONE);
	}

}
