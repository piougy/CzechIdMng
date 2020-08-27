package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.text.MessageFormat;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModelProperty;

/**
 * Request for validate or change password. DTO is not persisted
 * to database. DTO is used only for transfer information from a end system
 * to IdM. For example password filter
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccPasswordFilterRequestDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = GuardedStringDeserializer.class)
	@ApiModelProperty(notes = "New password.", dataType = "string")
	private GuardedString password;

	@NotEmpty
	@ApiModelProperty(notes = "Identifier of identity on end system. The attribute may be sAMAccountName.", dataType = "string")
	private String username;

	@NotEmpty
	@ApiModelProperty(notes = "Resource system identifier.", dataType = "string")
	private String resource;

	@ApiModelProperty(notes = "Log identifier that connect request from end system to IdM.", dataType = "string")
	private String logIdentifier;

	@ApiModelProperty(notes = "Password filter version from end system to IdM.", dataType = "string")
	private String version;

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getLogIdentifier() {
		return logIdentifier;
	}

	public void setLogIdentifier(String logIdentifier) {
		this.logIdentifier = logIdentifier;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Return metadata for log.
	 *
	 * @return
	 */
	public String getLogMetadata() {
		StringBuilder result = new StringBuilder();
		if (StringUtils.isNotBlank(getLogIdentifier())) {
			result.append(MessageFormat.format("Log identifier: [{0}]. ", getLogIdentifier()));
		}
		if (StringUtils.isNotBlank(getVersion())) {
			result.append(MessageFormat.format("Password filter version: [{0}].", getVersion()));
		}
		return StringUtils.defaultString(result.toString());
	}
}
