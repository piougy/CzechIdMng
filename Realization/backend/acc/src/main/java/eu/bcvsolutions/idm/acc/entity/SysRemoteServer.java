package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringSerializer;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 * Remote server with connectors.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Entity
@Table(name = "sys_remote_server")
public class SysRemoteServer extends AbstractEntity implements IcConnectorServer {
	
	private static final long serialVersionUID = 1L;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "host", length = DefaultFieldLengths.NAME, nullable = false)
	private String host;
	
	@Audited
	@Column(name = "port")
	private int port;
	
	@Transient
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	@JsonSerialize(using = GuardedStringSerializer.class)
	private transient GuardedString password;
	
	@Audited
	@NotNull
	@Column(name = "use_ssl", nullable = false)
	private boolean useSsl;
	
	@Audited
	@Column(name = "timeout")
	private int timeout = 3600;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
