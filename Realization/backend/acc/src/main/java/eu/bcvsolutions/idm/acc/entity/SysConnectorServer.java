package eu.bcvsolutions.idm.acc.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringSerializer;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 *	Default implementation with remote connector servers persisted with target system
 *	Basic info about server (host, port, key)	
 *
 *	@author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Audited
@Embeddable
public class SysConnectorServer implements IcConnectorServer, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "host")
	private String host;
	
	@Column(name = "port")
	private int port;
	
	@Transient
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	@JsonSerialize(using = GuardedStringSerializer.class)
	private transient GuardedString password;
	
	@NotNull
	@Column(name = "use_ssl", nullable = false)
	private boolean useSsl;
	
	@Column(name = "timeout")
	private int timeout = 3600;

	public boolean isUseSsl() {
		return useSsl;
	}

	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	@Override
	public String getFullServerName() {
		return this.getHost() + IcConnectorInstance.SERVER_NAME_DELIMITER + this.getPort();
	}
}
