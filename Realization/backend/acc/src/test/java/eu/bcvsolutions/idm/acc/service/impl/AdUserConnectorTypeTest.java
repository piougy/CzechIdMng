package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockAdUserConnectorType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for CSV connector type.
 *
 * @author Vít Švanda
 */
@Transactional
public class AdUserConnectorTypeTest extends AbstractIntegrationTest {

	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmEntityStateService entityStateService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}


	@Test
	public void testStepOne() {
		ConnectorType connectorType = connectorManager.getConnectorType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testCreateUser() {
		ConnectorType connectorType = connectorManager.getConnectorType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdUserConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		String testUserName = stepExecutedResult.getMetadata().get(MockAdUserConnectorType.TEST_USERNAME_KEY);
		String createdTestUserName = stepExecutedResult.getMetadata().get(MockAdUserConnectorType.TEST_CREATED_USER_DN_KEY);
		assertNotNull(createdTestUserName);
		assertEquals(testUserName, createdTestUserName);
		
		// Clean
		systemService.delete(systemDto);
	}
	
	@Test
	public void testDeleteUser() {
		ConnectorType connectorType = connectorManager.getConnectorType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdUserConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		IdmEntityStateDto entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);

		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_DELETE_USER_TEST);
		// Execute step for testing permissions to delete user.
		connectorManager.execute(connectorTypeDto);
		entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNull(entityStateDto);
		
		// Clean
		systemService.delete(systemDto);
	}
	
	@Test
	public void testAssignUserToGroup() {
		ConnectorType connectorType = connectorManager.getConnectorType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdUserConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		IdmEntityStateDto entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);

		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_ASSIGN_GROUP_TEST);
		// Execute step for testing permissions to assign user to the group.
		connectorManager.execute(connectorTypeDto);
		entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);
		
		// Clean
		entityStateService.delete(entityStateDto);
		systemService.delete(systemDto);
	}
	
	@Test
	public void testStepFour() {
		
		ConnectorType connectorType = connectorManager.getConnectorType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		String newUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.NEW_USER_CONTAINER_KEY, newUserContainerMock);
		String userContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.USER_SEARCH_CONTAINER_KEY, userContainerMock);
		String deletedUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.DELETE_USER_CONTAINER_KEY, deletedUserContainerMock);
		String domainMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.DOMAIN_KEY, domainMock);
		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);
		//  Execute step four.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		// Check containers on the system's operationOptions.
		systemDto = systemService.get(systemDto.getId());
		IcConnectorInstance connectorInstance = systemService.getConnectorInstance(systemDto);
		IdmFormDefinitionDto operationOptionsFormDefinition = systemService.getOperationOptionsConnectorFormDefinition(connectorInstance);
		String newUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.NEW_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(newUserContainerMock, newUserContainer);
		String deletedUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.DELETE_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(deletedUserContainerMock, deletedUserContainer);
		String searchUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.USER_SEARCH_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(userContainerMock, searchUserContainer);
		String domain = getValueFromConnectorInstance(MockAdUserConnectorType.DOMAIN_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(domainMock, domain);

		// Check created schema attributes.
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());
		List<SysSchemaAttributeDto> attributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.NAME.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.PASSWORD.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.ENABLE.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> MockAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> MockAdUserConnectorType.SAM_ACCOUNT_NAME_ATTRIBUTE.equals(attribute.getName())));

		// Check created schema attributes.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemId(systemDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.NAME.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.PASSWORD.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.ENABLE.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> MockAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> MockAdUserConnectorType.SAM_ACCOUNT_NAME_ATTRIBUTE.equals(attribute.getName())));
		
		// Clean
		systemService.delete(systemDto);
	}

	private void generateMockSchema(SysSystemDto systemDto) {
		SysSchemaObjectClassDto schemaAccount = new SysSchemaObjectClassDto();
		schemaAccount.setSystem(systemDto.getId());
		schemaAccount.setObjectClassName(IcObjectClassInfo.ACCOUNT);
		schemaAccount = schemaService.save(schemaAccount);
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(IcAttributeInfo.NAME);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
	}

	private SysSystemDto createSystem(String systemName, ConnectorTypeDto connectorTypeDto) {

		connectorTypeDto.setReopened(false);
		connectorManager.load(connectorTypeDto);
		assertNotNull(connectorTypeDto);

		String fakeHost = this.getHelper().createName();

		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.HOST, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.PORT, "636");
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.USER, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.PASSWORD, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SSL_SWITCH, "false");
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_NAME, systemName);
		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_ONE);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);
		BaseDto systemDto = stepExecutedResult.getEmbedded().get(MockAdUserConnectorType.SYSTEM_DTO_KEY);
		assertNotNull("System ID cannot be null!", systemDto);
		SysSystemDto system = systemService.get(systemDto.getId());
		assertNotNull(system);
		return system;
	}

	protected String getValueFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			return (String) values.get(0).getValue();
		}
		return null;
	}

}
