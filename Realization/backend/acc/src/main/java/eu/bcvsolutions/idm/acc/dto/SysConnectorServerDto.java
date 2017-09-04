package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 * DTO for {@link SysConnectorServer}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SysConnectorServerDto implements IcConnectorServer, Serializable {

	private static final long serialVersionUID = 8434045947764847844L;
	
	private String host;
	private int port;
	private transient GuardedString password;
	private boolean useSsl;
	private int timeout = 3600;

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

	@Override
	public String getFullServerName() {
		return this.getHost() + IcConnectorInstance.SERVER_NAME_DELIMITER + this.getPort();
	}

}
