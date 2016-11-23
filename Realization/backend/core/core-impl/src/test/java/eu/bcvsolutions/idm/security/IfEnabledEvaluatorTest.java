package eu.bcvsolutions.idm.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;
import eu.bcvsolutions.idm.security.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.security.exception.ModuleDisabledException;
import eu.bcvsolutions.idm.security.service.impl.IfEnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for {@link IfEnabledEvaluator}
 * 
 * @author Radek Tomiška
 *
 */
public class IfEnabledEvaluatorTest extends AbstractUnitTest {

	private static final String MODULE_ONE = "test-module-one";
	private static final String MODULE_TWO = "test-module-two";
	private static final String PROPERTY_ONE = "test-property-one";
	private static final String PROPERTY_TWO = "test-property-two";

	@Mock
	private IdmConfigurationService configurationService;

	@Mock
	private ModuleService moduleService;

	private IfEnabledEvaluator evaluator;

	@Before
	public void init() {
		evaluator = new IfEnabledEvaluator(moduleService, configurationService);
	}

	/**
	 * Constructs test {@link IfEnabled} annotation.
	 * 
	 * @param modules
	 * @param properties
	 * @param asValue if given modules will be set to value or module annotation property.
	 * @return
	 */
	private IfEnabled prepareAnnotation(String[] modules, String[] properties, boolean asValue) {
		return new IfEnabled() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return IfEnabled.class;
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
		evaluator.checkIsEnabled(null, null, prepareAnnotation(null, null, false));
	}

	@Test
	public void testEnabledModuleOne() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		try {
			evaluator.checkIsEnabled(null, null, prepareAnnotation(new String[]{ MODULE_ONE}, null, false));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testDisabledModuleOne() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		try {
			evaluator.checkIsEnabled(null, null, prepareAnnotation(new String[]{ MODULE_ONE}, null, false));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}
	
	@Test
	public void testEnabledModuleOneAsValue() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		
		evaluator.checkIsEnabled(null, null, prepareAnnotation(new String[]{ MODULE_ONE}, null, true));
		
		verify(moduleService).isEnabled(MODULE_ONE);
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testDisabledModuleOneAsValue() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(false);
		try {
			evaluator.checkIsEnabled(null, null, prepareAnnotation(new String[]{ MODULE_ONE}, null, true));
		} finally {
			verify(moduleService).isEnabled(MODULE_ONE);
		}
	}	
	
	@Test
	public void testEnabledPropertyOne() {
		when(configurationService.getBooleanValue(PROPERTY_ONE, false)).thenReturn(true);
		
		evaluator.checkIsEnabled(null, null, prepareAnnotation(null, new String[]{ PROPERTY_ONE}, true));
		
		verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
	}
	
	
	@Test(expected = ConfigurationDisabledException.class)
	public void testDisabledPropertyOne() {
		when(configurationService.getBooleanValue(PROPERTY_ONE, false)).thenReturn(false);
		
		try {
			evaluator.checkIsEnabled(null, null, prepareAnnotation(null, new String[]{ PROPERTY_ONE}, true));
		} finally {
			verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
		}
	}
	
	@Test(expected = ModuleDisabledException.class)
	public void testAnyDisabledModule() {
		when(moduleService.isEnabled(MODULE_ONE)).thenReturn(true);
		when(moduleService.isEnabled(MODULE_TWO)).thenReturn(false);
		try {
			evaluator.checkIsEnabled(null, null, prepareAnnotation(new String[]{ MODULE_ONE, MODULE_TWO}, null, true));
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
			evaluator.checkIsEnabled(null, null, prepareAnnotation(null, new String[]{ PROPERTY_ONE, PROPERTY_TWO}, true));
		} finally {
			verify(configurationService).getBooleanValue(PROPERTY_ONE, false);
			verify(configurationService).getBooleanValue(PROPERTY_TWO, false);
		}
	}

}
