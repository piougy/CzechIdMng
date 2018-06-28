package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.exception.ModuleNotDisableableException;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultModuleService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultModuleServiceUnitTest extends AbstractVerifiableUnitTest {

	private static final List<List<GroupPermission>> ALL_PERMISSIONS;
	private static final List<List<ResultCode>> ALL_RESULT_CODES;

	static {
		ALL_PERMISSIONS = new ArrayList<>();
		ALL_PERMISSIONS.add(Arrays.asList(CoreGroupPermission.values()));
		ALL_PERMISSIONS.add(Arrays.asList(NotificationGroupPermission.values()));
		//
		ALL_RESULT_CODES = new ArrayList<>();
		ALL_RESULT_CODES.add(Arrays.asList(CoreResultCode.values()));
	}

	@Mock
	private ConfigurationService configurationService;

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
		ALL_PERMISSIONS.stream().forEach((permissions) -> {
			allPermissions.addAll(permissions);
		});

		Assert.assertTrue(allPermissions.containsAll(defaultModuleService.getAvailablePermissions()));
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testAvailablePermissionOneEnabled() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(false);

		Assert.assertTrue(ALL_PERMISSIONS.get(0).containsAll(defaultModuleService.getAvailablePermissions()));
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test(expected = ModuleNotDisableableException.class)
	public void testIsDisableable() {
		defaultModuleService.disable(ModuleDescriptorOne.MODULE_ID);
	}

	@Test
	public void testEnabledModules() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(true);

		Assert.assertEquals(2, defaultModuleService.getEnabledModules().size());
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testDisabledModules() {
		when(configurationService.getBooleanValue(any(String.class), any(Boolean.class))).thenReturn(false);

		Assert.assertEquals(1, defaultModuleService.getEnabledModules().size());
		Assert.assertEquals(2, defaultModuleService.getInstalledModules().size());
		verify(configurationService).getBooleanValue(any(String.class), any(Boolean.class));
	}

	@Test
	public void testUninstalledModule() {
		Assert.assertFalse(defaultModuleService.isEnabled("not_exists"));
	}
	
	@Test
	public  void testResultCodes() {
		// TODO: move result codes into manager - move filter from rest to service
		Assert.assertTrue(new ModuleDescriptorOne().getResultCodes().isEmpty());
		//
		List<ResultCode> allResultCodes = new ArrayList<>();
		ALL_RESULT_CODES.stream().forEach((resultCodes) -> {
			allResultCodes.addAll(resultCodes);
		});
		Assert.assertTrue(allResultCodes.containsAll(new ModuleDescriptorTwo().getResultCodes()));
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
		
		@Override
		public List<ResultCode> getResultCodes() {
			return ALL_RESULT_CODES.get(0);
		}
	}
}
