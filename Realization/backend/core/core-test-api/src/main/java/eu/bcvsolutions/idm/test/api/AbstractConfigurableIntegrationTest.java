package eu.bcvsolutions.idm.test.api;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Test all configurable services to provide configuration properties - "formal" test.
 * 
 * Use in your custom module to check module provided configurations (see CoreConfigurableIntegrationTest as example).
 * 
 * @author Radek Tomiška
 * @since 9.7.7
 *
 */
@Ignore
public class AbstractConfigurableIntegrationTest extends AbstractIntegrationTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractConfigurableIntegrationTest.class);
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	@Autowired private EnabledEvaluator enabledEvaluator;
	
	@Test
	public void testCheckRegisteredConfigurations() {
		List<Configurable> configurables = context
				.getBeansOfType(Configurable.class)
				.values()
				.stream()
				.filter(c -> enabledEvaluator.isEnabled(c))
				.collect(Collectors.toList());
		//
		Assert.assertFalse(configurables.isEmpty());
		//
		// check all configuration provides it's type
		Assert.assertTrue(configurables.stream().allMatch(c -> StringUtils.isNotBlank(c.getConfigurableType())));
		// check all configuration provides it's name
		Assert.assertTrue(configurables.stream().allMatch(c -> {
			String name = c.getName();
			if (name.contains(".")) {
				LOG.warn("Configurable component [{}] has dynamic name [{}]. Define configurable name to better localization support.",
						c.getConfigurableType(), name);
			}
			return StringUtils.isNotBlank(name);
		}));
		// check all configuration provides it's properties (not nullable)
		Assert.assertTrue(configurables.stream().allMatch(c -> c.getPropertyNames() != null));
		// check all configuration has correct namespace if it's secured 
		Assert.assertTrue(configurables.stream().allMatch(c -> {
			// can be other (e.g. not configurable from UI) => only invalid way is checked
			if (c.isSecured()) {
				return !c.getConfigurationPrefix().startsWith(ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX);
			}
			return !c.getConfigurationPrefix().startsWith(ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX);
		}));
		//
		// check not disablable configuration cannot be disabled
		Assert.assertTrue(configurables.stream().allMatch(c -> {
			// can be other (e.g. not configurable from UI) => only invalid way is checked
			if (!c.isDisableable()) {
				//  try to disable configuration
				try {
					configurationService.setBooleanValue(c.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
					return !c.isDisabled();
				} finally {
					configurationService.deleteValue(c.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED));
				}
			}
			return true;
		}));
		// 
		// check required core common configurations are available (has to be available for all modules)
		Assert.assertTrue(configurables.stream().anyMatch(c -> c.getConfigurableType().equals("identity")));
		Assert.assertTrue(configurables.stream().anyMatch(c -> c.getConfigurableType().equals("role")));
		Assert.assertTrue(configurables.stream().anyMatch(c -> c.getConfigurableType().equals("tree")));
		Assert.assertTrue(configurables.stream().anyMatch(c -> c.getConfigurableType().equals("attachment")));
	}
}
