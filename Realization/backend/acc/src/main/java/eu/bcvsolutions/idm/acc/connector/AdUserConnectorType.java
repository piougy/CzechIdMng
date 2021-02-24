package eu.bcvsolutions.idm.acc.connector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationInactiveOwnerBehaviorType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.Pair;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.utils.CertificateUtils;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;

/**
 * AD wizard for users.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
@Component(AdUserConnectorType.NAME)
public class AdUserConnectorType extends DefaultConnectorType {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdUserConnectorType.class);

	public static final String NAME = "ad-connector-type";
	private static final String STEP_ONE = "stepOne";
	private static final String STEP_FOUR = "stepFour";
	private static final String STEP_CREATE_USER_TEST = "stepCreateUserTest";
	private static final String STEP_DELETE_USER_TEST = "stepDeleteUserTest";
	private static final String STEP_ASSIGN_GROUP_TEST = "stepAssignToGroupTest";
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String USER = "user";
	private static final String SSL_SWITCH = "sslSwitch";
	private static final String SSL = "ssl";
	private static final String PRINCIPAL = "principal";
	private static final String CREDENTIALS = "credentials";
	private static final String PASSWORD = "password";
	protected static final String SYSTEM_NAME = "name";
	private static final String SCHEMA_ID_KEY = "schemaId";
	private static final String CRT_ATTACHMENT_ID_KEY = "attachmentId";
	private static final String CRT_SUBJECT_DN_KEY = "subjectDN";
	private static final String CRT_FINGER_PRINT_KEY = "fingerPrint";
	private static final String CRT_VALIDITY_FROM_KEY = "crtValidityFrom";
	private static final String CRT_VALIDITY_TILL_KEY = "crtValidityTill";
	private static final String CRT_FILE_PATH_KEY = "crtFilePath";
	private static final String SERVER_CRT_ATTACHMENT_ID_KEY = "serverAttachmentId";
	private static final String SERVER_CRT_SUBJECT_DN_KEY = "serverSubjectDN";
	private static final String SERVER_CRT_FINGER_PRINT_KEY = "serverFingerPrint";
	private static final String SERVER_CRT_VALIDITY_FROM_KEY = "serverCrtValidityFrom";
	private static final String SERVER_CRT_VALIDITY_TILL_KEY = "serverCrtValidityTill";
	private static final String HAS_TRUSTED_CA_KEY = "hasTrustedCa";
	private static final String TEST_USERNAME_KEY = "testUserName";
	private static final String TEST_USER_CONTAINER_KEY = "userContainer";
	private static final String TEST_GROUP_KEY = "testGroup";
	private static final String TEST_CREATED_USER_DN_KEY = "testCreatedUserDN";
	private static final String ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY = "entityStateWithTestCreatedUserDN";
	private static final String ENTRY_OBJECT_CLASSES_KEY = "accountObjectClasses";
	private static final String OBJECT_CLASSES_TO_SYNC_KEY = "objectClassesToSynchronize";
	private static final String VLV_SORT_ATTRIBUTE_KEY = "vlvSortAttribute";
	private static final String USE_VLV_SORT_KEY = "useVlvControls";
	private static final String PAGE_SIZE_KEY = "pageSize";
	private static final String DEFAULT_UID_KEY = "defaultIdAttribute";
	private static final String BASE_CONTEXT_USER_KEY = "userBaseContexts";
	private static final String ROOT_SUFFIXES_KEY = "baseContextsToSynchronize";
	private static final String PAIRING_SYNC_SWITCH_KEY = "pairingSyncSwitch";
	private static final String PROTECTED_MODE_SWITCH_KEY = "protectedModeSwitch";
	private static final String PAIRING_SYNC_DN_ATTR_KEY = "pairingSyncEavDnAttribute";
	private static final String MAPPING_SYNC_ID = "mappingSyncId";
	private static final String PAIRING_SYNC_ID = "pairingSyncId";
	public static final String USER_SEARCH_CONTAINER_KEY = "searchUserContainer";
	public static final String NEW_USER_CONTAINER_KEY = "newUserContainer";
	public static final String DELETE_USER_CONTAINER_KEY = "deleteUserContainer";
	public static final String DOMAIN_KEY = "domainContainer";
	public static final String LDAP_GROUPS_ATTRIBUTE = "ldapGroups";

	// Default values
	private static final String[] ENTRY_OBJECT_CLASSES_DEFAULT_VALUES = {"top", "user", "person", "organizationalPerson"};
	public static final String SAM_ACCOUNT_NAME_ATTRIBUTE = "sAMAccountName";
	public static final String PAIRING_SYNC_DN_ATTR_DEFAULT_VALUE = "distinguishedName";
	private static final int PAGE_SIZE_DEFAULT_VALUE = 100;
	private static final String PAIRING_SYNC_NAME = "Pairing sync";

	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private IdmEntityStateService entityStateService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private IdmFormAttributeService formAttributeService;

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.ad.ADConnector";
	}

	@Override
	public String getIconKey() {
		return "ad-connector-icon";
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MS AD - Users", 1));
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

		IdmFormDefinitionDto operationOptionsFormDefinition = this.getSystemService().getOperationOptionsConnectorFormDefinition(systemDto);
		if (operationOptionsFormDefinition != null) {
			// Find attribute with domain.
			metadata.put(DOMAIN_KEY, getValueFromConnectorInstance(DOMAIN_KEY, systemDto, operationOptionsFormDefinition));
			// Find attribute with container with existed users.
			metadata.put(USER_SEARCH_CONTAINER_KEY, getValueFromConnectorInstance(USER_SEARCH_CONTAINER_KEY, systemDto, operationOptionsFormDefinition));
			// Find attribute with container with new users.
			metadata.put(NEW_USER_CONTAINER_KEY, getValueFromConnectorInstance(NEW_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition));
			// Find attribute with container with deleted users.
			metadata.put(DELETE_USER_CONTAINER_KEY, getValueFromConnectorInstance(DELETE_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition));
		}

		// Load the provisioning mapping.
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		SysSystemMappingDto mappingDto = systemMappingService.find(mappingFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(SysSystemMappingDto::getCreated))
				.orElse(null);
		if (mappingDto != null) {
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
			connectorType.getMetadata().put(MAPPING_ID, mappingDto.getId().toString());
			connectorType.getMetadata().put(PROTECTED_MODE_SWITCH_KEY, String.valueOf(mappingDto.isProtectionEnabled()));
		}

		// Load the provisioning mapping.
		SysSystemMappingFilter syncMappingFilter = new SysSystemMappingFilter();
		syncMappingFilter.setSystemId(systemDto.getId());
		syncMappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		SysSystemMappingDto syncMappingDto = systemMappingService.find(syncMappingFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(SysSystemMappingDto::getCreated))
				.orElse(null);
		if (syncMappingDto != null) {
			connectorType.getMetadata().put(MAPPING_SYNC_ID, syncMappingDto.getId().toString());
		}

		// Load the pairing sync (beware by name!).
		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setSystemId(systemDto.getId());
		syncFilter.setName(PAIRING_SYNC_NAME);

		AbstractSysSyncConfigDto syncDto = syncConfigService.find(syncFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(AbstractDto::getCreated))
				.orElse(null);
		if (syncDto != null) {
			connectorType.getMetadata().put(PAIRING_SYNC_ID, syncDto.getId().toString());
		}

		IdmEntityStateFilter entityStateFilter = new IdmEntityStateFilter();
		entityStateFilter.setOwnerId(systemDto.getId());
		entityStateFilter.setOwnerType(entityStateManager.getOwnerType(systemDto.getClass()));
		entityStateFilter.setResultCode(AccResultCode.WIZARD_AD_CREATED_TEST_USER_DN.getCode());

		IdmEntityStateDto entityStateDto = entityStateManager.findStates(entityStateFilter, null).stream().findFirst().orElse(null);
		Object dn = null;
		if (entityStateDto != null
				&& entityStateDto.getResult() != null
				&& entityStateDto.getResult().getModel() != null
				&& entityStateDto.getResult().getModel().getParameters() != null) {
			dn = entityStateDto.getResult().getModel().getParameters().get(TEST_CREATED_USER_DN_KEY);
		}

		if (dn instanceof String) {
			String testUserDN = (String) dn;
			connectorType.getMetadata().put(ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY, entityStateDto.getId().toString());
			connectorType.getMetadata().put(TEST_CREATED_USER_DN_KEY, testUserDN);
		}

		return connectorType;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		try {
			super.execute(connectorType);
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
		}
		return connectorType;
	}

	/**
	 * Execute first step of AD wizard.
	 */
	private void executeStepOne(ConnectorTypeDto connectorType) {
		String port = connectorType.getMetadata().get(PORT);
		Assert.notNull(port, "Port cannot be null!");
		String host = connectorType.getMetadata().get(HOST);
		Assert.notNull(host, "Host cannot be null!");
		String user = connectorType.getMetadata().get(USER);
		Assert.notNull(user, "Username cannot be null!");
		String sslSwitch = connectorType.getMetadata().get(SSL_SWITCH);
		Assert.notNull(sslSwitch, "SSL switch cannot be null!");
		String password = connectorType.getMetadata().get(PASSWORD);

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		SysSystemDto systemDto;
		boolean create = true;
		if (systemId != null) {
			// System already exists.
			create = false;
			systemDto = getSystemService().get(UUID.fromString(systemId), IdmBasePermission.READ);
		} else {
			// Create new system.
			systemDto = new SysSystemDto();
			// System is set as readOnly only if is new.
			systemDto.setReadonly(true);
		}
		systemDto.setName(connectorType.getMetadata().get(SYSTEM_NAME));
		// Resolve remote system.
		systemDto.setRemoteServer(connectorType.getRemoteServer());
		// Find connector key and set it to the system.
		IcConnectorKey connectorKey = getConnectorManager().findConnectorKey(connectorType);
		Assert.notNull(connectorKey, "Connector key was not found!");
		systemDto.setConnectorKey(new SysConnectorKeyDto(connectorKey));
		// Check permission on create or update system (others permissions will be not checked (EAV for identity, mapping, sync, entity state ...)).
		systemDto = getSystemService().save(systemDto, create ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE);

		// Put new system to the connector type (will be returned to FE).
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Set the port.
		this.setValueToConnectorInstance(PORT, port, systemDto, connectorFormDef);
		// Set the host.
		this.setValueToConnectorInstance(HOST, host, systemDto, connectorFormDef);
		// Set the user.
		this.setValueToConnectorInstance(PRINCIPAL, user, systemDto, connectorFormDef);
		// Set the SSL switch.
		this.setValueToConnectorInstance(SSL, sslSwitch, systemDto, connectorFormDef);
		// Set the password.
		// Password is mandatory only if none exists in connector configuration.
		String passwordInSystem = this.getValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);
		if (Strings.isNotBlank(password) && !GuardedString.SECRED_PROXY_STRING.equals(password)) {
			this.setValueToConnectorInstance(CREDENTIALS, password, systemDto, connectorFormDef);
		} else {
			Assert.notNull(passwordInSystem, "Password cannot be null!");
		}

		// Find domain DN.
		if (Strings.isBlank(connectorType.getMetadata().get(DOMAIN_KEY))) {
			String defaultNamingContext = this.findDnsHostName("389", host, user, password, false);
			connectorType.getMetadata().put(DOMAIN_KEY, defaultNamingContext);
		}
		// Find Users container DN.
		String usersContainerDN = this.findDn("(&(CN=Users)(objectClass=container))", "389", host, user, password, false);
		connectorType.getMetadata().put(TEST_USER_CONTAINER_KEY, usersContainerDN);
		// Find Domain Users group DN.
		String domainUsersDN = this.findDn("(&(CN=Domain Guests)(objectClass=group))", "389", host, user, password, false);
		connectorType.getMetadata().put(TEST_GROUP_KEY, domainUsersDN);
		// Generate random test user name.
		connectorType.getMetadata().put(TEST_USERNAME_KEY,
				MessageFormat.format("TestUserIdM_{0}", UUID.randomUUID().toString().substring(0, 8))
		);

		if (!Boolean.parseBoolean(sslSwitch)) {
			// LDAPS is trun off, this step will be skipped.
			return;
		}
		Pair<X509Certificate, Boolean> serverCertificatePair = getServerCertificate(port, host);

		if (serverCertificatePair != null) {
			boolean hasTrustedCA = serverCertificatePair.getValue();
			// Put information if the server already has trusted certificate. 
			connectorType.getMetadata().put(HAS_TRUSTED_CA_KEY, String.valueOf(hasTrustedCA));

			X509Certificate serverCertificate = serverCertificatePair.getKey();
			X509Certificate resultCertificate = getCertificateFromAD(serverCertificate, port, host, user, password);
			if (resultCertificate != null) {
				try {
					// Save CA as file.
					File caFile = new File(Paths.get(getTrustedCaFolder(), getCaFileName(resultCertificate)).toString());
					FileUtils.copyInputStreamToFile(CertificateUtils.certificateToPem(resultCertificate), caFile);
					// Save certificate as a temporary attachment.
					IdmAttachmentDto attachment = new IdmAttachmentDto();
					attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
					attachment.setName("AD_CA");
					attachment.setMimetype("application/x-pem-file");
					attachment.setInputData(CertificateUtils.certificateToPem(resultCertificate));
					attachment = attachmentManager.saveAttachment(null, attachment);

					// Save server certificate as a temporary attachment.
					IdmAttachmentDto serverAttachment = new IdmAttachmentDto();
					serverAttachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
					serverAttachment.setName("SERVER_AD_CA");
					serverAttachment.setMimetype("application/x-pem-file");
					serverAttachment.setInputData(CertificateUtils.certificateToPem(serverCertificate));
					serverAttachment = attachmentManager.saveAttachment(null, serverAttachment);

					// Put data to connectorType for FE
					connectorType.getMetadata().put(CRT_ATTACHMENT_ID_KEY, attachment.getId().toString());
					connectorType.getMetadata().put(CRT_SUBJECT_DN_KEY, resultCertificate.getSubjectDN().getName());
					connectorType.getMetadata().put(CRT_VALIDITY_FROM_KEY, getZonedDateTime(resultCertificate.getNotBefore()));
					connectorType.getMetadata().put(CRT_VALIDITY_TILL_KEY, getZonedDateTime(resultCertificate.getNotAfter()));
					// Fingerprint by SHA1 (is use in windows certificate manager)
					connectorType.getMetadata().put(CRT_FINGER_PRINT_KEY, DigestUtils.sha1Hex(resultCertificate.getEncoded()));
					connectorType.getMetadata().put(CRT_FILE_PATH_KEY, Paths.get(caFile.getAbsolutePath()).toString());

					// Put data to connectorType for FE
					connectorType.getMetadata().put(SERVER_CRT_ATTACHMENT_ID_KEY, serverAttachment.getId().toString());
					connectorType.getMetadata().put(SERVER_CRT_SUBJECT_DN_KEY, serverCertificate.getSubjectDN().getName());
					connectorType.getMetadata().put(SERVER_CRT_VALIDITY_FROM_KEY, getZonedDateTime(serverCertificate.getNotBefore()));
					connectorType.getMetadata().put(SERVER_CRT_VALIDITY_TILL_KEY, getZonedDateTime(serverCertificate.getNotAfter()));
					// Fingerprint by SHA1 (is use in windows certificate manager)
					connectorType.getMetadata().put(SERVER_CRT_FINGER_PRINT_KEY, DigestUtils.sha1Hex(serverCertificate.getEncoded()));
				} catch (CertificateException | IOException ex) {
					throw new CoreException(ex.getLocalizedMessage(), ex);
				}
			}
		}
	}

	/**
	 * Execute test for check permissions for create account/user on the AD.
	 */
	private void executeCreateUserTest(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);

		String testUser = connectorType.getMetadata().get(TEST_USERNAME_KEY);
		Assert.notNull(testUser, "Test username cannot be null!");
		String usersContainer = connectorType.getMetadata().get(TEST_USER_CONTAINER_KEY);
		Assert.notNull(usersContainer, "Test user container cannot be null!");

		// Check exist of container on the AD.
		String usersContainerDN = this.findDn(
				MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))",
						usersContainer)
				, port, host, user, password, ssl);
		if (Strings.isBlank(usersContainerDN)) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
					ImmutableMap.of("dn", usersContainer
					)
			);
		}
		String createdUserDN = createTestUser(testUser, usersContainerDN, port, host, user, password, ssl);
		// As protection against unauthorized deletion of a user other than the one
		// created, the DN on the BE will be in the entity state.
		IdmEntityStateDto entityStateWithTestUser = createEntityStateWithTestUser(systemDto, createdUserDN);
		connectorType.getMetadata().put(ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY, entityStateWithTestUser.getId().toString());
		connectorType.getMetadata().put(TEST_CREATED_USER_DN_KEY, createdUserDN);
	}

	/**
	 * Execute permission test for assign user to group.
	 */
	private void executeAssignTestUserToGroup(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);

		String testUser = connectorType.getMetadata().get(TEST_USERNAME_KEY);
		Assert.notNull(testUser, "Test username cannot be null!");
		String entityStateId = connectorType.getMetadata().get(ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		Assert.notNull(entityStateId, "Entity state ID with created test user DN cannot be null!");

		// Find Domain Users group DN.
		String groupDN = this.findDn("(&(CN=Domain Guests)(objectClass=group))", port, host, user, password, ssl);
		if (Strings.isBlank(groupDN)) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_GROUP_NOT_FOUND,
					ImmutableMap.of("dn", groupDN
					)
			);
		}

		// As protection against unauthorized deletion of a user other than the one
		// created, the DN will be loaded from the entity state.
		IdmEntityStateDto entityStateDto = entityStateService.get(entityStateId);
		Assert.notNull(entityStateDto, "Entity state with created test user DN cannot be null!");
		ResultModel model = entityStateDto.getResult().getModel();
		Object dn = model.getParameters().get(TEST_CREATED_USER_DN_KEY);
		Assert.isTrue(dn instanceof String, "Test domain users cannot be null!");
		String testUserDN = (String) dn;

		// Assign test user to the group.
		assignTestUserToGroup(testUserDN, groupDN, port, host, user, password, ssl);
	}

	/**
	 * Execute permission test for delete user from AD.
	 */
	private void executeDeleteUserTest(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);
		String entityStateId = connectorType.getMetadata().get(ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		Assert.notNull(entityStateId, "Entity state ID with created test user DN cannot be null!");

		// As protection against unauthorized deletion of a user other than the one
		// created, the DN will be loaded from the entity state.
		IdmEntityStateDto entityStateDto = entityStateService.get(entityStateId);
		Assert.notNull(entityStateDto, "Entity state with created test user DN cannot be null!");
		ResultModel model = entityStateDto.getResult().getModel();
		Object dn = model.getParameters().get(TEST_CREATED_USER_DN_KEY);
		Assert.isTrue(dn instanceof String, "Test domain users cannot be null!");
		String testDomainUsers = (String) dn;

		// Delete test user from AD.
		deleteTestUser(testDomainUsers, port, host, user, password, ssl);
		// Delete entity state.
		entityStateService.delete(entityStateDto);
	}

	/**
	 * Step for filling additional information as connector (OU) DNs. Enable protected mode. Add pairing sync.
	 */
	private void executeStepFour(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		String pairingSyncId = connectorType.getMetadata().get(PAIRING_SYNC_ID);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
		boolean pairingSyncSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(PAIRING_SYNC_SWITCH_KEY));
		String pairingSyncAttributeCode = connectorType.getMetadata().get(PAIRING_SYNC_DN_ATTR_KEY);
		if (pairingSyncAttributeCode != null) {
			Assert.notNull(pairingSyncAttributeCode, "EAV code pairing sync cannot be null!");
		}
		boolean protectedModeSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(PROTECTED_MODE_SWITCH_KEY));

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);

		String domainContainer = connectorType.getMetadata().get(DOMAIN_KEY);
		Assert.notNull(domainContainer, "Domain cannot be null!");
		String newUserContainer = connectorType.getMetadata().get(NEW_USER_CONTAINER_KEY);
		Assert.notNull(newUserContainer, "Container for new users cannot be null!");
		String searchUserContainer = connectorType.getMetadata().get(USER_SEARCH_CONTAINER_KEY);
		Assert.notNull(searchUserContainer, "Container for search users cannot be null!");
		String deleteUserContainer = connectorType.getMetadata().get(DELETE_USER_CONTAINER_KEY);

		String newUserContainerAD = this.findDn(
				MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))", newUserContainer)
				, port, host, user, password, ssl);
		if (Strings.isBlank(newUserContainerAD)) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
					ImmutableMap.of("dn", newUserContainer
					)
			);
		}
		String searchUserContainerAD = this.findDn(
				MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))", searchUserContainer)
				, port, host, user, password, ssl);
		if (Strings.isBlank(searchUserContainerAD)) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
					ImmutableMap.of("dn", searchUserContainer
					)
			);
		}

		if (Strings.isNotBlank(deleteUserContainer)) {
			String deleteUserContainerAD = this.findDn(
					MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))", deleteUserContainer)
					, port, host, user, password, ssl);
			if (Strings.isBlank(deleteUserContainerAD)) {
				throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
						ImmutableMap.of("dn", deleteUserContainer
						)
				);
			}
		}

		IdmFormDefinitionDto operationOptionsFormDefinition = getSystemService().getOperationOptionsConnectorFormDefinition(systemDto);
		if (operationOptionsFormDefinition != null) {
			// Set domain to system's operation options.
			operationOptionsFormDefinition = initFormAttributeDefinition(operationOptionsFormDefinition, DOMAIN_KEY, (short) 3);
			setValueToConnectorInstance(DOMAIN_KEY, domainContainer, systemDto, operationOptionsFormDefinition);
			// Set container for new users to system's operation options.
			operationOptionsFormDefinition = initFormAttributeDefinition(operationOptionsFormDefinition, NEW_USER_CONTAINER_KEY, (short) 4);
			setValueToConnectorInstance(NEW_USER_CONTAINER_KEY, newUserContainer, systemDto, operationOptionsFormDefinition);
			// Set container for deleted users to system's operation options.
			operationOptionsFormDefinition = initFormAttributeDefinition(operationOptionsFormDefinition, DELETE_USER_CONTAINER_KEY, (short) 5);
			setValueToConnectorInstance(DELETE_USER_CONTAINER_KEY, deleteUserContainer, systemDto, operationOptionsFormDefinition);
			// Set container for exists users to system's operation options.
			operationOptionsFormDefinition = initFormAttributeDefinition(operationOptionsFormDefinition, USER_SEARCH_CONTAINER_KEY, (short) 6);
			setValueToConnectorInstance(USER_SEARCH_CONTAINER_KEY, searchUserContainer, systemDto, operationOptionsFormDefinition);
		}

		boolean reopened = connectorType.isReopened();
		if (!reopened) {
			// This attributes will be updated only for first time.
			initDefaultConnectorSettings(systemDto, connectorFormDef);
		}

		// Attributes below will updated everytime (for reopen system too).

		// Base context for search users.
		// We need to searching in all containers (for new, existed and deleted users). So all three values will be use in the base context.
		List<Serializable> values = Lists.newArrayList(Sets.newHashSet(searchUserContainer, newUserContainer, deleteUserContainer));
		this.setValueToConnectorInstance(BASE_CONTEXT_USER_KEY, values, systemDto, connectorFormDef);
		// Root suffixes.
		// First we have to find root DN (only DCs) and generate the schema for it.
		String root = getRoot(searchUserContainer);
		this.setValueToConnectorInstance(ROOT_SUFFIXES_KEY, root, systemDto, connectorFormDef);

		// Check system (execute a connector test)
		try {
			this.getSystemService().checkSystem(systemDto);
		} catch (ConnectorException ex) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_TEST_FAILED,
					ImmutableMap.of("system", systemDto.getName(), "ex", ex.getLocalizedMessage()), ex);
		}
		// Generate a system schema.
		generateSchema(connectorType, systemDto);
		// Second we will set full user search base DN and again generate the schema.
		this.setValueToConnectorInstance(ROOT_SUFFIXES_KEY, searchUserContainer, systemDto, connectorFormDef);
		SysSchemaObjectClassDto schemaDto = generateSchema(connectorType, systemDto);

		// Find sAMAccountName attribute in the schema.
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaDto.getId());
		schemaAttributeFilter.setSystemId(systemDto.getId());
		schemaAttributeFilter.setName(SAM_ACCOUNT_NAME_ATTRIBUTE);
		SysSchemaAttributeDto sAMAccountNameAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (sAMAccountNameAttribute == null) {
			// Attribute missing -> create it now.
			sAMAccountNameAttribute = createSchemaAttribute(schemaDto, SAM_ACCOUNT_NAME_ATTRIBUTE, String.class.getName(), true, true, false);
		}
		// Find __ENABLE__ attribute in the schema.
		schemaAttributeFilter.setName(IcAttributeInfo.ENABLE);
		SysSchemaAttributeDto enableAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (enableAttribute == null) {
			// Attribute missing -> create it now.
			createSchemaAttribute(schemaDto, IcAttributeInfo.ENABLE, Boolean.class.getName(), true, true, false);
		}

		// Find __PASSWORD__ attribute in the schema.
		schemaAttributeFilter.setName(IcAttributeInfo.PASSWORD);
		SysSchemaAttributeDto passwordAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (passwordAttribute == null) {
			// Attribute missing -> create it now.
			createSchemaAttribute(schemaDto, IcAttributeInfo.PASSWORD, GuardedString.class.getName(), false, false, false);
		}
		// Find Ldap groups attribute in the schema.
		schemaAttributeFilter.setName(LDAP_GROUPS_ATTRIBUTE);
		SysSchemaAttributeDto ldapGroupsAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (ldapGroupsAttribute == null) {
			// Attribute missing -> create it now.
			createSchemaAttribute(schemaDto, LDAP_GROUPS_ATTRIBUTE, String.class.getName(), true, true, true);
		}

		String mappingId = connectorType.getMetadata().get(MAPPING_ID);
		if (mappingId == null) {
			// Create identity mapping for provisioning.
			SysSystemMappingDto mappingDto = new SysSystemMappingDto();
			mappingDto.setObjectClass(schemaDto.getId());
			mappingDto.setOperationType(SystemOperationType.PROVISIONING);
			mappingDto.setEntityType(SystemEntityType.IDENTITY);
			mappingDto.setName("AD users provisioning mapping.");
			mappingDto.setProtectionEnabled(protectedModeSwitch);
			mappingDto = systemMappingService.publish(
					new SystemMappingEvent(
							SystemMappingEvent.SystemMappingEventType.CREATE,
							mappingDto,
							ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, Boolean.TRUE)))
					.getContent();
			mappingDto = systemMappingService.save(mappingDto);
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
			connectorType.getMetadata().put(DefaultConnectorType.MAPPING_ID, mappingDto.getId().toString());
		} else {
			SysSystemMappingDto mappingDto = systemMappingService.get(UUID.fromString(mappingId));
			// If protected mode switch changed, then mapping will be updated.
			if (mappingDto.isProtectionEnabled() != protectedModeSwitch) {
				mappingDto.setProtectionEnabled(protectedModeSwitch);
				mappingDto = systemMappingService.save(mappingDto);
			}
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
		}

		if (pairingSyncSwitch) {
			createPairingSync(connectorType, pairingSyncAttributeCode, schemaDto, schemaAttributeFilter, sAMAccountNameAttribute);
		}
		if (pairingSyncId != null) {
			// If is protected mode activated, then set strategy to LINK_PROTECTED, otherwise set DO_NOT_LINK.
			AbstractSysSyncConfigDto pairingSync = syncConfigService.get(UUID.fromString(pairingSyncId));
			if (pairingSync instanceof SysSyncIdentityConfigDto) {
				SysSyncIdentityConfigDto sync = (SysSyncIdentityConfigDto) pairingSync;
				if (protectedModeSwitch) {
					sync.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
				} else {
					sync.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
				}
				syncConfigService.save(sync);
			}
		}
	}

	/**
	 * Init default connector configurations.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		this.setValueToConnectorInstance(VLV_SORT_ATTRIBUTE_KEY, SAM_ACCOUNT_NAME_ATTRIBUTE, systemDto, connectorFormDef);
		// Set the VLV page size attribute.
		this.setValueToConnectorInstance(PAGE_SIZE_KEY, PAGE_SIZE_DEFAULT_VALUE, systemDto, connectorFormDef);
		// Default UID key.
		this.setValueToConnectorInstance(DEFAULT_UID_KEY, SAM_ACCOUNT_NAME_ATTRIBUTE, systemDto, connectorFormDef);
	}

	/**
	 * Creates pairing sync mapping, schema attributes, default role and sync.
	 */
	private void createPairingSync(ConnectorTypeDto connectorType, String pairingSyncAttributeCode, SysSchemaObjectClassDto schemaDto, SysSchemaAttributeFilter schemaAttributeFilter, SysSchemaAttributeDto sAMAccountNameAttribute) {
		boolean protectedModeSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(PROTECTED_MODE_SWITCH_KEY));

		// Create identity DN EAV attribute if missing (for sync of groups).
		IdmFormAttributeDto attribute = getFormService().getAttribute(IdmIdentity.class, pairingSyncAttributeCode);
		if (attribute == null) {
			attribute = new IdmFormAttributeDto(pairingSyncAttributeCode, "Distinguished name (it is one-time attribute!)", PersistentType.SHORTTEXT);
			attribute.setDescription("This attribute is one-time and is used only for the initial pairing synchronization of accounts and groups. " +
					"The DN in this attribute may not be currently valid.");
			getFormService().saveAttribute(IdmIdentity.class, attribute);
		}

		// Create identity mapping for pairing sync.
		String mappingSyncId = connectorType.getMetadata().get(MAPPING_SYNC_ID);
		if (mappingSyncId == null) {
			SysSystemMappingDto mappingDto = new SysSystemMappingDto();
			mappingDto.setObjectClass(schemaDto.getId());
			mappingDto.setOperationType(SystemOperationType.SYNCHRONIZATION);
			mappingDto.setEntityType(SystemEntityType.IDENTITY);
			mappingDto.setName("Pairing sync mapping");
			mappingDto = systemMappingService.save(mappingDto);
			mappingSyncId = mappingDto.getId().toString();
			connectorType.getMetadata().put(MAPPING_SYNC_ID, mappingSyncId);
		}
		
		// Create identity mapping for pairing sync.
		String pairingSyncId = connectorType.getMetadata().get(PAIRING_SYNC_ID);
		if (pairingSyncId == null) {
			// Creates default role with that system.
			IdmRoleDto roleDto = this.createRoleSystem(connectorType);
			connectorType.getMetadata().put(SKIP_CREATES_ROLE_WITH_SYSTEM, Boolean.TRUE.toString());
			Assert.notNull(roleDto, "Role cannot be null!");

			schemaAttributeFilter.setName(IcAttributeInfo.NAME);
			SysSchemaAttributeDto nameSchemaAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
					.stream()
					.findFirst()
					.orElse(null);
			Assert.notNull(nameSchemaAttribute, "Attribute __NAME__ cannot be null!");

			// Create DN -> EAV attribute
			SysSystemAttributeMappingDto dnEavMappingAttribute = new SysSystemAttributeMappingDto();
			dnEavMappingAttribute.setEntityAttribute(false);
			dnEavMappingAttribute.setExtendedAttribute(true);
			dnEavMappingAttribute.setIdmPropertyName(pairingSyncAttributeCode);
			dnEavMappingAttribute.setSchemaAttribute(nameSchemaAttribute.getId());
			dnEavMappingAttribute.setUid(false);
			dnEavMappingAttribute.setSystemMapping(UUID.fromString(mappingSyncId));
			dnEavMappingAttribute.setCached(true);
			dnEavMappingAttribute.setName("DN");
			systemAttributeMappingService.save(dnEavMappingAttribute);

			// Create the correlation attribute.
			SysSystemAttributeMappingDto correlationAttribute = new SysSystemAttributeMappingDto();
			correlationAttribute.setEntityAttribute(true);
			correlationAttribute.setExtendedAttribute(false);
			correlationAttribute.setIdmPropertyName(IdmIdentity_.username.getName());
			correlationAttribute.setSchemaAttribute(sAMAccountNameAttribute.getId());
			correlationAttribute.setUid(true);
			correlationAttribute.setSystemMapping(UUID.fromString(mappingSyncId));
			correlationAttribute.setCached(true);
			correlationAttribute.setName(sAMAccountNameAttribute.getName());
			correlationAttribute = systemAttributeMappingService.save(correlationAttribute);
		
			SysSyncIdentityConfigDto syncIdentityConfigDto = new SysSyncIdentityConfigDto();
			syncIdentityConfigDto.setName(PAIRING_SYNC_NAME);
			syncIdentityConfigDto.setReconciliation(true);
			syncIdentityConfigDto.setSystemMapping(UUID.fromString(mappingSyncId));
			// If is protected mode activated, then set strategy to LINK_PROTECTED, otherwise set DO_NOT_LINK.
			if (protectedModeSwitch) {
				syncIdentityConfigDto.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
			} else {
				syncIdentityConfigDto.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
			}
			syncIdentityConfigDto.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
			syncIdentityConfigDto.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
			syncIdentityConfigDto.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
			syncIdentityConfigDto.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
			syncIdentityConfigDto.setDefaultRole(roleDto.getId());
			syncIdentityConfigDto.setCorrelationAttribute(correlationAttribute.getId());
			syncIdentityConfigDto = (SysSyncIdentityConfigDto) syncConfigService.save(syncIdentityConfigDto);
			syncIdentityConfigDto.setAssignDefaultRoleToAll(true);
			syncIdentityConfigDto.setStartAutoRoleRec(false);
			connectorType.getMetadata().put(PAIRING_SYNC_ID, syncIdentityConfigDto.getId().toString());
		}
	}

	/**
	 * Check attribute definition, if no exists, then will be created.
	 */
	private IdmFormDefinitionDto initFormAttributeDefinition(IdmFormDefinitionDto definitionDto, String code, Short order) {
		IdmFormAttributeDto attribute = definitionDto.getMappedAttributeByCode(code);
		if (attribute == null) {
			attribute = new IdmFormAttributeDto(code, code, PersistentType.SHORTTEXT);
			attribute.setFormDefinition(definitionDto.getId());
			attribute.setSeq(order);
			formAttributeService.save(attribute);
			definitionDto = getFormService().getDefinition(definitionDto.getId());
		}
		return definitionDto;
	}

	/**
	 * Create schema attribute.
	 */
	private SysSchemaAttributeDto createSchemaAttribute(SysSchemaObjectClassDto schemaDto, String attributeName, String type, boolean returnByDefault, boolean updateable, boolean multivalued) {
		SysSchemaAttributeDto attribute;
		attribute = new SysSchemaAttributeDto();
		attribute.setName(attributeName);
		attribute.setClassType(type);
		attribute.setObjectClass(schemaDto.getId());
		attribute.setCreateable(true);
		attribute.setReadable(true);
		attribute.setMultivalued(multivalued);
		attribute.setReturnedByDefault(returnByDefault);
		attribute.setUpdateable(updateable);

		return schemaAttributeService.save(attribute);
	}

	/**
	 * Generate schema.
	 */
	private SysSchemaObjectClassDto generateSchema(ConnectorTypeDto connectorType, SysSystemDto systemDto) {
		// Generate schema
		List<SysSchemaObjectClassDto> schemas = this.getSystemService().generateSchema(systemDto);
		SysSchemaObjectClassDto schemaAccount = schemas.stream()
				.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
				.orElse(null);
		Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");
		connectorType.getMetadata().put(SCHEMA_ID_KEY, schemaAccount.getId().toString());

		return schemaAccount;
	}

	/**
	 * Find root in container DN. -> return only DCs.
	 */
	private String getRoot(String searchUserContainer) {
		List<String> containers = Lists.reverse(Lists.newArrayList(searchUserContainer.split(",")));
		List<String> roots = Lists.newArrayList();

		StringBuilder stringBuilder = new StringBuilder();
		containers.forEach(container -> {
			if (container.trim().startsWith("DC") || container.trim().startsWith("dc")) {
				roots.add(container);
			}
		});
		Lists.reverse(roots).forEach(root -> {
			stringBuilder.append(root.trim());
			stringBuilder.append(',');
		});
		String root = stringBuilder.toString();
		// Remove last comma.
		if (Strings.isNotBlank(root) && root.contains(",")) {
			root = root.substring(0, root.length() - 1);
		}
		return root;
	}

	/**
	 * Get server certificates.
	 */
	private Pair<X509Certificate, Boolean> getServerCertificate(String port, String host) {
		try {
			SSLSocket socket = null;
			try {
				socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, Integer.parseInt(port));
				socket.startHandshake();
				LOG.info("Certificate is already trusted for connection to the AD.");

				SSLSession session = socket.getSession();
				Certificate[] peerCertificates = session.getPeerCertificates();
				if (peerCertificates.length > 0) {
					return new Pair<>((X509Certificate) peerCertificates[0], Boolean.TRUE);
				}
			} catch (SSLException e) {
				LOG.info("Certificate is not trusted for connection to the AD.");

				SSLContext context = SSLContext.getInstance("TLS");
				// Workaround how get server certificates from the AD server if the IdM server doesn't have trusted certificate yet.
				SavingTrustManager tm = new SavingTrustManager();
				context.init(null, new TrustManager[]{tm}, null);
				SSLSocketFactory factory = context.getSocketFactory();
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
				socket = (SSLSocket) factory.createSocket(host, Integer.parseInt(port));
				socket.setSoTimeout(10000);
				// Start handshake. In the case without check a trusted certificate.
				socket.startHandshake();

				X509Certificate[] chain = tm.chain;
				if (chain.length > 0) {
					return new Pair<>(chain[0], Boolean.FALSE);
				}
			} finally {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			}
		} catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
			throw new CoreException(ex.getLocalizedMessage(), ex);
		}
		return null;
	}


	/**
	 * Try to find authority for this certificate in AD (we want to return certificate with the biggest validity).
	 */
	private X509Certificate getCertificateFromAD(X509Certificate serverCrt, String port, String host, String user, String password) {
		X509Certificate resultCertificate = null;
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, "389", user, password, false);
			ldapContext = new InitialDirContext(ldapEnv);

			boolean continueSearching = true;
			resultCertificate = serverCrt;
			while (continueSearching) {
				X509Certificate authorityCrtOnAD = findAuthorityCrtOnAD(resultCertificate, ldapContext);
				if (authorityCrtOnAD != null) {
					// Validate certificate by found authority.
					try {
						CertificateUtils.verifyCertificate(resultCertificate, authorityCrtOnAD);
					} catch (CertificateException ex) {
						throw new ResultCodeException(AccResultCode.WIZARD_AD_CONNECTOR_CRT_NOT_TRUSTED,
								ImmutableMap.of("serialNumber", authorityCrtOnAD.getSerialNumber().toString(16).toUpperCase()
								), ex
						);
					}
				}
				if (authorityCrtOnAD == null) {
					// No authority certificate was found, previous certificate is result.
					continueSearching = false;
				} else if (authorityCrtOnAD.getIssuerDN() != null
						&& resultCertificate.getIssuerDN() != null
						&& resultCertificate.getIssuerDN().getName().equals(authorityCrtOnAD.getIssuerDN().getName())) {
					// Issuer name in previous and returned authority certificate is same -> returned certificate is result.
					resultCertificate = authorityCrtOnAD;
					continueSearching = false;
				} else if (authorityCrtOnAD.getIssuerDN() == null || Strings.isBlank(authorityCrtOnAD.getIssuerDN().getName())) {
					// Found authority certificate doesn't have issuer -> it is result.
					resultCertificate = authorityCrtOnAD;
					continueSearching = false;
				} else {
					// Next round.
					resultCertificate = authorityCrtOnAD;
				}
			}
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", serverCrt != null ? serverCrt.getSubjectDN().getName() : host)
					, ex
			);
		} catch (CertificateException ex) {
			throw new CoreException(ex.getLocalizedMessage(), ex);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
		return resultCertificate;
	}

	/**
	 * Create test certificates.
	 */
	private String createTestUser(String username, String entryDN, String port, String host, String adUser, String adPassword, boolean ssl) {
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, port, adUser, adPassword, ssl);
			ldapContext = new InitialDirContext(ldapEnv);

			// Entry's attributes.
			Attribute cn = new BasicAttribute("cn", username);
			Attribute oc = new BasicAttribute("objectClass");
			oc.add("top");
			oc.add("person");
			oc.add("organizationalPerson");
			oc.add("inetOrgPerson");

			// Build the entry  
			BasicAttributes entry = new BasicAttributes();
			entry.put(cn);
			entry.put(oc);

			// Add the entry.
			DirContext context = ldapContext.createSubcontext(MessageFormat.format("CN={0},{1}", username, entryDN), entry);
			Attributes attributes = context.getAttributes("");

			return (String) attributes.get("distinguishedname").get();
		} catch (NameAlreadyBoundException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_CONNECTOR_DN_ALREADY_EXISTS,
					ImmutableMap.of("dn", MessageFormat.format("CN={0},{1}", username, entryDN))
					, ex);
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", MessageFormat.format("CN={0},{1}", username, entryDN))
					, ex);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Delete the wizard test user.
	 */
	private void deleteTestUser(String entryDN, String port, String host, String adUser, String adPassword, boolean ssl) {
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, port, adUser, adPassword, ssl);
			ldapContext = new InitialDirContext(ldapEnv);
			// Delete the entry.
			ldapContext.destroySubcontext(entryDN);
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", entryDN
					), ex
			);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Assign the wizard test user to test group.
	 */
	private void assignTestUserToGroup(String userDN, String groupDN, String port, String host, String adUser, String adPassword, boolean ssl) {
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, port, adUser, adPassword, ssl);
			ldapContext = new InitialDirContext(ldapEnv);

			ModificationItem[] roleMods = new ModificationItem[]
					{
							new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", userDN))
					};
			ldapContext.modifyAttributes(groupDN, roleMods);
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", userDN
					), ex
			);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Find given DN. If not exists, then return null.
	 */
	private String findDn(String filter, String port, String host, String adUser, String adPassword, boolean ssl) {
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, port, adUser, adPassword, ssl);
			ldapContext = new InitialDirContext(ldapEnv);

			// Get the configuration naming context.
			Attributes ldapContextAttributes = ldapContext.getAttributes("");
			String defaultNamingContext = ldapContextAttributes.get("defaultNamingContext").get().toString();
			LOG.info("AD default naming context: {}.", defaultNamingContext);

			// Create the search controls
			SearchControls searchControls = new SearchControls();

			//Specify the search scope
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// Search for objects using the filter
			NamingEnumeration<SearchResult> result = ldapContext.search(defaultNamingContext,
					filter, searchControls);
			if (result.hasMoreElements()) {
				return MessageFormat.format("{0},{1}", result.next().getName(), defaultNamingContext);
			}
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", filter), ex
			);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
		return null;
	}

	/**
	 * Find dnsHostName (domain) on the AD.
	 */
	private String findDnsHostName(String port, String host, String adUser, String adPassword, boolean ssl) {
		DirContext ldapContext = null;
		try {
			// Init LDAP context.
			Hashtable<String, String> ldapEnv = getAdEnvironment(host, port, adUser, adPassword, ssl);
			ldapContext = new InitialDirContext(ldapEnv);

			// Get the configuration naming context.
			Attributes ldapContextAttributes = ldapContext.getAttributes("");
			return ldapContextAttributes.get("dnsHostName").get().toString();
		} catch (CommunicationException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_COMMUNICATION_EXCEPTION,
					ImmutableMap.of("host", host
					), ex
			);
		} catch (NamingException ex) {
			throw new ResultCodeException(AccResultCode.WIZARD_AD_OPERATION_FAILED,
					ImmutableMap.of("dn", "dnsHostName"), ex
			);
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
				} catch (NamingException e) {
					// Only log it.
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Compile LDAP environment properties.
	 */
	private Hashtable<String, String> getAdEnvironment(String host, String port, String user, String password, boolean ssl) {
		Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
		ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		// Use ldaps for SSL
		if (ssl) {
			ldapEnv.put(Context.PROVIDER_URL, MessageFormat.format("ldaps://{0}:{1}", host, port));
		} else {
			ldapEnv.put(Context.PROVIDER_URL, MessageFormat.format("ldap://{0}:{1}", host, port));
		}
		ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		ldapEnv.put(Context.SECURITY_PRINCIPAL, user);
		ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
		return ldapEnv;
	}

	/**
	 * Find authority for given certificate on AD.
	 */
	private X509Certificate findAuthorityCrtOnAD(X509Certificate certificate, DirContext ldapContext) throws NamingException, CertificateException {
		LOG.info("Cert Subject DN: {}.", certificate.getSubjectDN().getName());
		LOG.info("Cert Issuer DN: {}.", certificate.getIssuerDN().getName());
		String issuer = certificate.getIssuerDN().getName();
		if (Strings.isNotBlank(issuer)) {
			// Issuer exist, we will try to get certificate of the authority from AD server.

			// Get the configuration naming context.
			Attributes ldapContextAttributes = ldapContext.getAttributes("");
			String configurationNamingContext = ldapContextAttributes.get("configurationNamingContext").get().toString();
			LOG.info("AD Configuration naming context: {}.", configurationNamingContext);

			// Create the search controls
			SearchControls searchControls = new SearchControls();

			//Specify the attributes to return
			String[] returnedAttributes = {"cACertificate"};
			searchControls.setReturningAttributes(returnedAttributes);

			//Specify the search scope
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// Search for objects using the filter
			NamingEnumeration<SearchResult> result = ldapContext.search(configurationNamingContext,
					MessageFormat.format("(&(cACertificateDN={0})(objectClass=pKIEnrollmentService))", issuer), searchControls);

			//Loop through the search results
			while (result.hasMoreElements()) {
				SearchResult sr = result.next();
				Attributes attrs = sr.getAttributes();
				Attribute caAttribute = attrs.get(returnedAttributes[0]);
				if (caAttribute != null) {
					Object value = caAttribute.get();
					if (value instanceof byte[]) {
						byte[] bytes = (byte[]) value;
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

						return CertificateUtils.getCertificate509(byteArrayInputStream);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Workaround how get server certificates from the AD server if the IdM server doesn't have trusted certificate yet.
	 */
	private static class SavingTrustManager implements X509TrustManager {

		private X509Certificate[] chain;

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			this.chain = chain;
		}
	}


	/**
	 * Create entity state for wizard test user
	 */
	private IdmEntityStateDto createEntityStateWithTestUser(SysSystemDto systemDto, String createdUserDN) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entityId", systemDto.getId());
		// Mark state with created user DN.
		parameters.put(TEST_CREATED_USER_DN_KEY, createdUserDN);

		DefaultResultModel resultModel = new DefaultResultModel(AccResultCode.WIZARD_AD_CREATED_TEST_USER_DN, parameters);
		IdmEntityStateDto entityStateDto = new IdmEntityStateDto();
		entityStateDto.setResult(
				new OperationResultDto
						.Builder(OperationState.CREATED)
						.setModel(resultModel)
						.build());
		return entityStateManager.saveState(systemDto, entityStateDto);
	}

	/**
	 * Convert Date to the zoned date time in string.
	 */
	private String getZonedDateTime(Date date) {
		String zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toString();

		return zonedDateTime.split("\\[")[0];
	}

	/**
	 * Folder with trusted certificates.
	 */
	private String getTrustedCaFolder() {
		return Paths.get(attachmentManager.getStoragePath(), "trustedCA").toString();
	}

	/**
	 * Generate file name for given certificate.
	 */
	private String getCaFileName(X509Certificate crt) {
		String subjectDn = SpinalCase.format(crt.getSubjectDN().getName());
		if (subjectDn.length() > 20) {
			subjectDn = subjectDn.substring(0, 19);
		}
		return MessageFormat.format("{0}_{1}.pem",
				subjectDn,
				crt.getSerialNumber().toString(16).toUpperCase());
	}

	@Override
	public int getOrder() {
		return 200;
	}

}
