package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncRoleConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import org.apache.logging.log4j.util.Strings;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * AD wizard for groups.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Component(AdGroupConnectorType.NAME)
public class AdGroupConnectorType extends AdUserConnectorType {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdGroupConnectorType.class);
	protected static final String MEMBER_SYSTEM_MAPPING = "memberSystemMappingId";
	private static final String GROUP_SYNC_ID = "groupSyncId";
	private static final String GROUP_CONTAINER_KEY = "groupContainer";
	public static final String OBJECT_GUID_ATTRIBUTE = "objectGUID";
	private static final String UID_FOR_GROUP_ATTRIBUTE = "gidAttribute";
	private static final String BASE_CONTEXT_GROUP_KEY = "groupBaseContexts";

	// Default values
	private static final String[] ENTRY_OBJECT_CLASSES_DEFAULT_VALUES = {"top", "group"};
	private static final int PAGE_SIZE_DEFAULT_VALUE = 100;
	private static final String CN_VALUE = "cn";

	// Connector type ID.
	public static final String NAME = "ad-group-connector-type";

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.ad.ADConnector";
	}

	@Override
	public String getIconKey() {
		return "ad-connector-icon";
	}
	
	@Override
	protected String getSchemaType() {
		return IcObjectClassInfo.GROUP;
	}


	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("test-MS AD - Groups", 1));
		metadata.put(PORT, "636");
		metadata.put(PAIRING_SYNC_DN_ATTR_KEY, PAIRING_SYNC_DN_ATTR_DEFAULT_VALUE);
		metadata.put(PROTECTED_MODE_SWITCH_KEY, "false");
		return metadata;
	}

	@Override
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		super.load(connectorType);
		if (!connectorType.isReopened()) {
			return connectorType;
		}
		// Load the system.
		SysSystemDto systemDto = (SysSystemDto) connectorType.getEmbedded().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemDto, "System must exists!");
		connectorType.getMetadata().put(SYSTEM_NAME, systemDto.getName());
		Map<String, String> metadata = connectorType.getMetadata();

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Find attribute with port.
		metadata.put(PORT, getValueFromConnectorInstance(PORT, systemDto, connectorFormDef));
		// Find attribute with host.
		metadata.put(HOST, getValueFromConnectorInstance(HOST, systemDto, connectorFormDef));
		// Find attribute with user.
		metadata.put(USER, getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef));
		// Find attribute with ssl switch.
		metadata.put(SSL_SWITCH, getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));

		return connectorType;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		try {
			//super.super.execute(connectorType);
			if (STEP_ONE.equals(connectorType.getWizardStepName())) {
				executeStepOne(connectorType);
			} else if (STEP_CREATE_USER_TEST.equals(connectorType.getWizardStepName())) {
				executeCreateUserTest(connectorType);
			} else if (STEP_DELETE_USER_TEST.equals(connectorType.getWizardStepName())) {
				executeDeleteUserTest(connectorType);
			} else if (STEP_ASSIGN_GROUP_TEST.equals(connectorType.getWizardStepName())) {
				executeAssignTestUserToGroup(connectorType);
			} else if (STEP_FOUR.equals(connectorType.getWizardStepName())) {
				executeStepFour(connectorType);
			} else {
				// Default loading of system DTO.
				String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
				Assert.notNull(systemId, "System ID cannot be null!");
				SysSystemDto systemDto = this.getSystemService().get(systemId);
				connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
			}
		} catch (ResultCodeException ex) {
			if (ex.getCause() instanceof AuthenticationException) {
				throw new ResultCodeException(AccResultCode.WIZARD_AD_AUTHENTICATION_FAILED, ex.getCause());
			}
			if (ex.getCause() instanceof CommunicationException) {
				CommunicationException exCause = (CommunicationException) ex.getCause();
				if (exCause.getRootCause() instanceof UnknownHostException) {
					UnknownHostException rootCause = (UnknownHostException) exCause.getRootCause();
					throw new ResultCodeException(AccResultCode.WIZARD_AD_UNKNOWN_HOST,
							ImmutableMap.of("host", rootCause.getLocalizedMessage()), ex.getCause());
				}
			}
			throw ex;
		} catch (IllegalArgumentException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,
					ImmutableMap.of("value", ex.getLocalizedMessage()), ex);
		}
		return connectorType;
	}

	/**
	 * Execute first step of AD wizard.
	 */
	protected void executeStepOne(ConnectorTypeDto connectorType) {
		String memberSystemMappingId = connectorType.getMetadata().get(MEMBER_SYSTEM_MAPPING);

		SysSystemMappingDto systemMappingDto = null;
		if (memberSystemMappingId != null) {
			systemMappingDto = getSystemMappingService().get(UUID.fromString(memberSystemMappingId), IdmBasePermission.READ);
		}
		if (systemMappingDto != null) {
			SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
			Assert.notNull(objectClassDto, "Schema DTO cannot be null!");
			SysSystemDto memberSystemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
			Assert.notNull(memberSystemDto, "Member system DTO cannot be null!");

			ConnectorType memberConnectorType = getConnectorManager().findConnectorTypeBySystem(memberSystemDto);
			if (!(memberConnectorType instanceof AdUserConnectorType)) {
				throw new ResultCodeException(
						AccResultCode.WIZARD_AD_GROUP_WRONG_MEMBER_CONNECTOR_TYPE,
						ImmutableMap.of("connectorType", memberConnectorType == null ? "none" : memberConnectorType.toString()
						)
				);
			}

			ConnectorTypeDto adUserSystemMockConnectorType = new ConnectorTypeDto();
			adUserSystemMockConnectorType.setReopened(true);
			adUserSystemMockConnectorType.getEmbedded().put(SYSTEM_DTO_KEY, memberSystemDto);
			adUserSystemMockConnectorType.getMetadata().put(SYSTEM_DTO_KEY, memberSystemDto.getId().toString());
			adUserSystemMockConnectorType = super.load(adUserSystemMockConnectorType);
			Map<String, String> metadata = connectorType.getMetadata();
			// Find attribute with port.
			metadata.put(PORT, adUserSystemMockConnectorType.getMetadata().get(PORT));
			// Find attribute with host.
			metadata.put(HOST, adUserSystemMockConnectorType.getMetadata().get(HOST));
			// Find attribute with user.
			metadata.put(USER, adUserSystemMockConnectorType.getMetadata().get(USER));
			// Find attribute with ssl switch.
			metadata.put(SSL_SWITCH, adUserSystemMockConnectorType.getMetadata().get(SSL_SWITCH));
			// Load password.
			IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(memberSystemDto);
			metadata.put(PASSWORD, this.getConfidentialValueFromConnectorInstance(CREDENTIALS, memberSystemDto, connectorFormDef));
		}
		super.executeStepOne(connectorType);
		// Get test group and find parent group container. Will be used as default group container.
		String testGroup = connectorType.getMetadata().get(TEST_GROUP_KEY);
		connectorType.getMetadata().put(GROUP_CONTAINER_KEY, getParent(testGroup));
	}

	/**
	 * Step for filling additional information as connector (OU) DNs. Add pairing sync.
	 */
	private void executeStepFour(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		String groupSyncId = connectorType.getMetadata().get(GROUP_SYNC_ID);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
		//boolean pairingSyncSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(PAIRING_SYNC_SWITCH_KEY));
//		String pairingSyncAttributeCode = connectorType.getMetadata().get(PAIRING_SYNC_DN_ATTR_KEY);
//		if (pairingSyncAttributeCode == null) {
//			pairingSyncAttributeCode = PAIRING_SYNC_DN_ATTR_DEFAULT_VALUE;
//		}

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);
		
		String groupContainer = connectorType.getMetadata().get(GROUP_CONTAINER_KEY);
		Assert.notNull(groupContainer, "Container with groups cannot be null!");

		String groupContainerAD = this.findDn(
				MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))", groupContainer)
				, port, host, user, password, ssl);
		if (Strings.isBlank(groupContainerAD)) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
					ImmutableMap.of("dn", groupContainer
					)
			);
		}

		IdmFormDefinitionDto operationOptionsFormDefinition = getSystemService().getOperationOptionsConnectorFormDefinition(systemDto);
		if (operationOptionsFormDefinition != null) {
			// Set container with groups to system's operation options.
			operationOptionsFormDefinition = initFormAttributeDefinition(operationOptionsFormDefinition, GROUP_CONTAINER_KEY, (short) 3);
			setValueToConnectorInstance(GROUP_CONTAINER_KEY, groupContainer, systemDto, operationOptionsFormDefinition);
		}

		String mappingSyncId = connectorType.getMetadata().get(MAPPING_SYNC_ID);
		String mappingId = connectorType.getMetadata().get(MAPPING_ID);
		if (mappingId == null && mappingSyncId == null) {
			// This attributes will be updated only if system doesn't have mapping.
			// Checking by existing mapping and not by reopen flag solves a problem with reopen wizard for to early closed wizard.
			// For example in the certificate step.
			initDefaultConnectorSettings(systemDto, connectorFormDef);
		}
		
		// Base context for search groups.
		// We need to searching in all containers. So group container will be use in the base context.
		List<Serializable> values = Lists.newArrayList(Sets.newHashSet(groupContainer));
		this.setValueToConnectorInstance(BASE_CONTEXT_GROUP_KEY, values, systemDto, connectorFormDef);
		
		// Set root suffixes and generate a schema.
		SysSchemaObjectClassDto schemaDto = generateSchema(connectorType, systemDto, connectorFormDef, groupContainer, values);

		// Find sAMAccountName attribute in the schema.
//		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
//		schemaAttributeFilter.setObjectClassId(schemaDto.getId());
//		schemaAttributeFilter.setSystemId(systemDto.getId());
//		schemaAttributeFilter.setName(SAM_ACCOUNT_NAME_ATTRIBUTE);
//		SysSchemaAttributeDto sAMAccountNameAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
//				.stream()
//				.findFirst()
//				.orElse(null);
//		if (sAMAccountNameAttribute == null) {
//			// Attribute missing -> create it now.
//			sAMAccountNameAttribute = createSchemaAttribute(schemaDto, SAM_ACCOUNT_NAME_ATTRIBUTE, String.class.getName(), true, true, false);
//		}
		// Find __ENABLE__ attribute in the schema.
//		schemaAttributeFilter.setName(IcAttributeInfo.ENABLE);
//		SysSchemaAttributeDto enableAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
//				.stream()
//				.findFirst()
//				.orElse(null);
//		if (enableAttribute == null) {
//			// Attribute missing -> create it now.
//			createSchemaAttribute(schemaDto, IcAttributeInfo.ENABLE, Boolean.class.getName(), true, true, false);
//		}

		// Find __PASSWORD__ attribute in the schema.
//		schemaAttributeFilter.setName(IcAttributeInfo.PASSWORD);
//		SysSchemaAttributeDto passwordAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
//				.stream()
//				.findFirst()
//				.orElse(null);
//		if (passwordAttribute == null) {
//			// Attribute missing -> create it now.
//			createSchemaAttribute(schemaDto, IcAttributeInfo.PASSWORD, GuardedString.class.getName(), false, true, false);
//		} else {
//			passwordAttribute.setUpdateable(true);
//			getSchemaAttributeService().save(passwordAttribute);
//		}
		// Find Ldap groups attribute in the schema.
//		schemaAttributeFilter.setName(LDAP_GROUPS_ATTRIBUTE);
//		SysSchemaAttributeDto ldapGroupsAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
//				.stream()
//				.findFirst()
//				.orElse(null);
//		if (ldapGroupsAttribute == null) {
//			// Attribute missing -> create it now.
//			createSchemaAttribute(schemaDto, LDAP_GROUPS_ATTRIBUTE, String.class.getName(), true, true, true);
//		}

		mappingId = connectorType.getMetadata().get(MAPPING_ID);
		if (mappingId == null) {
			// Create role mapping for sync.
			SysSystemMappingDto mappingDto = new SysSystemMappingDto();
			mappingDto.setObjectClass(schemaDto.getId());
			mappingDto.setOperationType(SystemOperationType.SYNCHRONIZATION);
			mappingDto.setEntityType(SystemEntityType.ROLE);
			mappingDto.setName("AD role sync mapping.");
			mappingDto = getSystemMappingService().publish(
					new SystemMappingEvent(
							SystemMappingEvent.SystemMappingEventType.CREATE,
							mappingDto,
							ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, Boolean.TRUE)))
					.getContent();
			mappingDto = getSystemMappingService().save(mappingDto);
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
			connectorType.getMetadata().put(DefaultConnectorType.MAPPING_ID, mappingDto.getId().toString());
		} else {
			SysSystemMappingDto mappingDto = getSystemMappingService().get(UUID.fromString(mappingId));
		
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
		}

//		if (pairingSyncSwitch) {
//		//	createPairingSync(connectorType, pairingSyncAttributeCode, schemaDto, schemaAttributeFilter, sAMAccountNameAttribute);
//		}
		if (groupSyncId != null) {
			// If is protected mode activated, then set strategy to LINK_PROTECTED, otherwise set DO_NOT_LINK.
			AbstractSysSyncConfigDto groupSync = getSyncConfigService().get(UUID.fromString(groupSyncId));
			if (groupSync instanceof SysSyncRoleConfigDto) {
				SysSyncRoleConfigDto sync = (SysSyncRoleConfigDto) groupSync;
			// todo
				getSyncConfigService().save(sync);
			}
		}
	}

	protected void initDefaultConnectorSettings(SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		// Set the entry object classes.
		List values = Lists.newArrayList(ENTRY_OBJECT_CLASSES_DEFAULT_VALUES);
		this.setValueToConnectorInstance(ENTRY_OBJECT_CLASSES_KEY, values, systemDto, connectorFormDef);
		// Set the object classes to sync.
		values = Lists.newArrayList(ENTRY_OBJECT_CLASSES_DEFAULT_VALUES);
		this.setValueToConnectorInstance(OBJECT_CLASSES_TO_SYNC_KEY, values, systemDto, connectorFormDef);
		// Set use VLV search.
		this.setValueToConnectorInstance(USE_VLV_SORT_KEY, Boolean.TRUE, systemDto, connectorFormDef);
		// Set the VLV attribute.
		this.setValueToConnectorInstance(VLV_SORT_ATTRIBUTE_KEY, CN_VALUE, systemDto, connectorFormDef);
		// Set the VLV page size attribute.
		this.setValueToConnectorInstance(PAGE_SIZE_KEY, PAGE_SIZE_DEFAULT_VALUE, systemDto, connectorFormDef);
		// Default UID key.
		this.setValueToConnectorInstance(DEFAULT_UID_KEY, OBJECT_GUID_ATTRIBUTE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance(UID_FOR_GROUP_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE, systemDto, connectorFormDef);
	}

	@Override
	public int getOrder() {
		return 200;
	}

}
