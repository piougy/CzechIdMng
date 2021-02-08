package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for systems.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class SysSystemFilter extends DataFilter {
	
	public static final String PARAMETER_VIRTUAL = "virtual";
	public static final String PARAMETER_PASSWORD_POLICY_VALIDATION_ID = "passwordPolicyValidationId";
	public static final String PARAMETER_PASSWORD_POLICY_GENERATION_ID = "passwordPolicyGenerationId";
	/**
	 * @deprecated @since 10.8.0 - standalone remove server agenda added
	 */
	@Deprecated
	public static final String PARAMETER_CONTAINS_REMOTE_SERVER_PASSWORD_PROXY_CHARS = "containsRemoteServerPasswordProxyChars";
	/**
	 * Remote server identifier.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_REMOTE_SERVER_ID = "remoteServerId";
	/**
	 * Remote server (true) or local (false) connectors are used.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_REMOTE = "remote";
	/**
	 * Connector framework.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_CONNECTOR_FRAMEWORK = "connectorFramework";
	/**
	 * Connector name.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_CONNECTOR_NAME = "connectorName";
	/**
	 * Connector bundle name.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_CONNECTOR_BUNDLE_NAME = "connectorBundleName";
	/**
	 * Connector bundle version.
	 * 
	 * @since 10.8.0
	 */
	public static final String PARAMETER_CONNECTOR_VERSION = "connectorVersion";
	
	
	public SysSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSystemFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemDto.class, data, parameterConverter);
	}

	public UUID getPasswordPolicyValidationId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_PASSWORD_POLICY_VALIDATION_ID);
	}

	public void setPasswordPolicyValidationId(UUID passwordPolicyValidationId) {
		set(PARAMETER_PASSWORD_POLICY_VALIDATION_ID, passwordPolicyValidationId);
	}

	public UUID getPasswordPolicyGenerationId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_PASSWORD_POLICY_GENERATION_ID);
	}

	public void setPasswordPolicyGenerationId(UUID passwordPolicyGenerationId) {
		set(PARAMETER_PASSWORD_POLICY_GENERATION_ID, passwordPolicyGenerationId);
	}

	public Boolean getVirtual() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_VIRTUAL);
	}

	public void setVirtual(Boolean virtual) {
		set(PARAMETER_VIRTUAL, virtual);
	}

	/**
	 * @deprecated @since 10.8.0 - standalone remove server agenda added
	 */
	@Deprecated
	public void setContainsRemoteServerPasswordProxyChars(Boolean savePassword) {
		set(PARAMETER_CONTAINS_REMOTE_SERVER_PASSWORD_PROXY_CHARS, savePassword);
	}

	/**
	 * @deprecated @since 10.8.0 - standalone remove server agenda added
	 */
	@Deprecated
	public Boolean isContainsRemoteServerPasswordProxyChars() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_CONTAINS_REMOTE_SERVER_PASSWORD_PROXY_CHARS);
	}
	
	/**
	 * Remote server with connectors.
	 * 
	 * @return remote server identifier
	 * @since 10.8.0
	 */
	public UUID getRemoteServerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_REMOTE_SERVER_ID);
	}

	/**
	 * Remote server with connectors.
	 * 
	 * @param remoteServerId remote server identifier
	 * @since 10.8.0
	 */
	public void setRemoteServerId(UUID remoteServerId) {
		set(PARAMETER_REMOTE_SERVER_ID, remoteServerId);
	}
	
	/**
	 * Remote connectors are used or not.
	 * 
	 * @return remote connector (true) or local (false).
	 * @since 10.8.0
	 */
	public Boolean getRemote() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_REMOTE);
	}

	/**
	 * Remote connectors are used or not.
	 * 
	 * @param remote remote connectors are used or not. (true) or local (false).
	 * @since 10.8.0
	 */
	public void setRemote(Boolean remote) {
		set(PARAMETER_REMOTE, remote);
	}
	
	/**
	 * Connector framework
	 * 
	 * @return connector framework
	 * @since 10.8.0
	 */
	public String getConnectorFramework() {
		return getParameterConverter().toString(getData(), PARAMETER_CONNECTOR_FRAMEWORK);
	}

	/**
	 * Connector framework.
	 * 
	 * @param connectorFramework connector framework
	 * @since 10.8.0
	 */
	public void setConnectorFramework(String connectorFramework) {
		set(PARAMETER_CONNECTOR_FRAMEWORK, connectorFramework);
	}
	
	/**
	 * Connector name.
	 * 
	 * @return connector name
	 * @since 10.8.0
	 */
	public String getConnectorName() {
		return getParameterConverter().toString(getData(), PARAMETER_CONNECTOR_NAME);
	}

	/**
	 * Connector name.
	 * 
	 * @param connectorName connector name
	 * @since 10.8.0
	 */
	public void setConnectorName(String connectorName) {
		set(PARAMETER_CONNECTOR_NAME, connectorName);
	}
	
	/**
	 * Connector bundle name
	 * 
	 * @return connector bundle name
	 * @since 10.8.0
	 */
	public String getConnectorBundleName() {
		return getParameterConverter().toString(getData(), PARAMETER_CONNECTOR_BUNDLE_NAME);
	}

	/**
	 * Connector bundle name
	 * 
	 * @param connectorBundleName bundle name
	 * @since 10.8.0
	 */
	public void setConnectorcBundleName(String connectorBundleName) {
		set(PARAMETER_CONNECTOR_BUNDLE_NAME, connectorBundleName);
	}
	
	/**
	 * Connector bundle version.
	 * 
	 * @return connector version
	 * @since 10.8.0
	 */
	public String getConnectorVersion() {
		return getParameterConverter().toString(getData(), PARAMETER_CONNECTOR_VERSION);
	}

	/**
	 * Connector bundle version.
	 * 
	 * @param connectorVersion connector version
	 * @since 10.8.0
	 */
	public void setConnectorVersion(String connectorVersion) {
		set(PARAMETER_CONNECTOR_VERSION, connectorVersion);
	}
}
