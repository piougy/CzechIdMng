package eu.bcvsolutions.idm.acc.service.impl;

import java.util.Arrays;
import java.util.Collections;

import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.connid.service.impl.ConnIdIcConnectorService;
import eu.bcvsolutions.idm.ic.connid.service.impl.DefaultIcConnectorFacadeFactory;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OperationOptionsIntegrationTest extends AbstractIntegrationTest {

	public static final String TEST_SYSTEM_NAME = "Operation_options_test_system_1";

	@Autowired
	DefaultAccTestHelper accTestHelper;

	@Autowired
	FormService formService;

	@Autowired
	SysSystemService systemService;

	@Spy
	@Autowired
	IcConnectorFacade connectorFacade;

	@Autowired
	DefaultSysSchemaObjectClassService sysSchemaObjectClassService;

	@Mock
	DefaultIcConnectorFacadeFactory mockFacadeFactory;

	@InjectMocks
	ConnIdIcConnectorService connIdIcConnectorService;

	@Mock
	ConnectorFacade mockConnector;


	@Test
	public void t0_testOperationOptionsCreation() {
		SysSystemDto system = accTestHelper.createSystem(TestResource.TABLE_NAME, TEST_SYSTEM_NAME);
		IdmFormDefinitionDto operationOptionsConnectorFormDefinition = systemService.getOperationOptionsConnectorFormDefinition(system);

		final String defName = DefaultSysSystemService.OPERATION_OPTIONS_DEFINITION_KEY + "-" + system.getConnectorKey().getFullName();
		IdmFormDefinitionDto definition = formService.getDefinition(SysSystem.class, defName);

		Assert.assertNotNull(definition);
		IdmFormInstanceDto formInstance = formService.getFormInstance(system, definition);
		Assert.assertEquals(operationOptionsConnectorFormDefinition, definition);

		Assert.assertNotNull(formInstance);

		Assert.assertNotNull(formInstance.getMappedAttributeByCode(OperationOptions.OP_PAGE_SIZE));
		Assert.assertNotNull(formInstance.getMappedAttributeByCode(OperationOptions.OP_ATTRIBUTES_TO_GET));
	}

	@Test
	public void t1_testOperationOptionsSetting() {
		Mockito.when(mockFacadeFactory.getConnectorFacade(Mockito.any(), Mockito.any())).thenReturn(mockConnector);
		SysSystemDto system = systemService.getByCode(TEST_SYSTEM_NAME);
		SysSystemMappingDto mapping = accTestHelper.createMapping(system);
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(system);
		IdmFormDefinitionDto operationOptionsConnectorFormDefinition = systemService.getOperationOptionsConnectorFormDefinition(system);

		IcObjectClassImpl icObjectClass = new IcObjectClassImpl(sysSchemaObjectClassService.get(mapping.getObjectClass()).getObjectClassName());
		connIdIcConnectorService.search(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, null, a -> true );
		Mockito.verify(mockFacadeFactory, Mockito.atLeastOnce()).getConnectorFacade(Mockito.any(), Mockito.any());
		Mockito.verify(mockConnector, Mockito.times(1)).search(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.argThat(operationOptions -> operationOptions.getOptions().isEmpty()));


		formService.saveValues(system, operationOptionsConnectorFormDefinition, OperationOptions.OP_PAGE_SIZE, Collections.singletonList(42069));
		formService.saveValues(system, operationOptionsConnectorFormDefinition, OperationOptions.OP_ATTRIBUTES_TO_GET, Arrays.asList("attr1", "attr2"));

		connIdIcConnectorService.search(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, null, a -> true );
		connIdIcConnectorService.createObject(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, Collections.emptyList() );
		connIdIcConnectorService.updateObject(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, new IcUidAttributeImpl("a", "a", "a"), Collections.emptyList() );
		connIdIcConnectorService.authenticateObject(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, "aaa", new GuardedString("a"));
		connIdIcConnectorService.deleteObject(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, new IcUidAttributeImpl("a", "a", "a"));
		connIdIcConnectorService.readObject(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, new IcUidAttributeImpl("a", "a", "a"));
		connIdIcConnectorService.synchronization(connectorInstance, systemService.getConnectorConfiguration(system), icObjectClass, null, a -> true);

		ArgumentMatcher<OperationOptions> optionsMatcher = operationOptions -> !operationOptions.getOptions().isEmpty()
				&& operationOptions.getPageSize() == 42069
				&& Arrays.asList(operationOptions.getAttributesToGet()).containsAll(Arrays.asList("attr1", "attr2"));

		Mockito.verify(mockConnector, Mockito.times(1)).search(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).getObject(Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).create(Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).update(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).delete(Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).sync(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
		Mockito.verify(mockConnector, Mockito.times(1)).authenticate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.argThat(optionsMatcher));
	}

}
