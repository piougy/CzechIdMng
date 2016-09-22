package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.AbstractUnitTest;
import eu.bcvsolutions.idm.core.exception.ModuleNotDisableableException;
import eu.bcvsolutions.idm.core.model.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultModuleService;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.security.domain.GroupPermission;
import eu.bcvsolutions.idm.security.service.impl.DefaultSecurityService;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class DefaultModuleServiceTest extends AbstractUnitTest {

	private static final List<List<GroupPermission>> ALL_PERMISSIONS;

	static {
		ALL_PERMISSIONS = new ArrayList<>();
		ALL_PERMISSIONS.add(Arrays.asList(IdmGroupPermission.values()));
		ALL_PERMISSIONS.add(Arrays.asList(NotificationGroupPermission.values()));
	}

	@Mock
	private IdmConfigurationService configurationService;

	private DefaultModuleService defaultModuleService;

	@Before
	public void init() {
		PluginRegistry<ModuleDescriptor, String> registry = SimplePluginRegistry
				.create(Lists.newArrayList(new ModuleDescriptorOne(), new ModuleDescriptorTwo()));
		defaultModuleService = new DefaultModuleService(registry, configurationService);
	}

	@Test
	public void testAvailablePermissionAllEnabled() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(true);

		List<GroupPermission> allPermissions = new ArrayList<>();
		for (List<GroupPermission> permissions : ALL_PERMISSIONS) {
			allPermissions.addAll(permissions);
		}

		assertTrue(allPermissions.containsAll(defaultModuleService.getAvailablePermissions()));
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testAvailablePermissionOneEnabled() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(false);

		assertTrue(ALL_PERMISSIONS.get(0).containsAll(defaultModuleService.getAvailablePermissions()));
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test(expected = ModuleNotDisableableException.class)
	public void testIsDisableable() {
		defaultModuleService.disable(ModuleDescriptorOne.MODULE_ID);
	}

	@Test
	public void testEnabledModules() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(true);

		assertEquals(2, defaultModuleService.getEnabledModules().size());
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testDisabledModules() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(false);

		assertEquals(1, defaultModuleService.getEnabledModules().size());
		assertEquals(2, defaultModuleService.getRegisteredModules().size());
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testUninstalledModule() {
		assertFalse(defaultModuleService.isEnabled("not_exists"));
	}

	private class ModuleDescriptorOne extends AbstractModuleDescriptor {

		public static final String MODULE_ID = "test_custom_one";

		@Override
		public String getId() {
			return MODULE_ID;
		}

		@Override
		public boolean isDisableable() {
			return false;
		}

		@Override
		public List<GroupPermission> getPermissions() {
			return ALL_PERMISSIONS.get(0);
		}
	}

	private class ModuleDescriptorTwo extends AbstractModuleDescriptor {

		public static final String MODULE_ID = "test_custom_two";

		@Override
		public String getId() {
			return MODULE_ID;
		}

		@Override
		public List<GroupPermission> getPermissions() {
			return ALL_PERMISSIONS.get(1);
		}
	}
}
