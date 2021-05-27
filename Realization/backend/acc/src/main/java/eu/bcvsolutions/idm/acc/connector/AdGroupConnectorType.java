package eu.bcvsolutions.idm.acc.connector;

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
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
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
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
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
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MS AD - Groups", 1));
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
//				executeCreateUserTest(connectorType);
//			} else if (STEP_DELETE_USER_TEST.equals(connectorType.getWizardStepName())) {
//				executeDeleteUserTest(connectorType);
//			} else if (STEP_ASSIGN_GROUP_TEST.equals(connectorType.getWizardStepName())) {
//				executeAssignTestUserToGroup(connectorType);
//			} else if (STEP_FOUR.equals(connectorType.getWizardStepName())) {
//				executeStepFour(connectorType);
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
	}


	@Override
	public int getOrder() {
		return 200;
	}

}
