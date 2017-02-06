package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 *	Default implementation with remote connector servers
 *	Basic info about server (name, url, port, key)	
 *
 *	@author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "sys_connector_server", indexes = { 
		@Index(name = "ux_connector_server_name", columnList = "name", unique = true)})
public class SysConnectorServer extends AbstractEntity implements IdentifiableByName, IcConnectorServer {

	private static final long serialVersionUID = -3814155757811719322L;
	
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "port")
	private int port;
	
	// TODO: confidential storage
	@Column(name = "password")
	private String password;
	
	@Column(name = "name_connector_bundle")
	private String nameConnectorBundle;
	
	@Column(name = "use_ssl")
	private boolean useSsl = false;
	
	@Column(name = "timeout")
	private int timeout = 3600;
	
	@Override
	public String getName() {
		return name;
	}
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNameConnectorBundle() {
		return nameConnectorBundle;
	}

	public void setNameConnectorBundle(String nameConnectorBundle) {
		this.nameConnectorBundle = nameConnectorBundle;
	}

	public void setName(String name) {
		this.name = name;
	}
}
