package eu.bcvsolutions.idm.acc.dto;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;

/**
 * Connector server - local or remote.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "connectorServers")
public class SysConnectorServerDto extends AbstractDto implements IcConnectorServer {

	private static final long serialVersionUID = 8434045947764847844L;
	//
	@Size(max = DefaultFieldLengths.NAME)
	private String host;
	private int port;
	private transient GuardedString password;
	private boolean useSsl;
	private int timeout = 3600;
	private boolean local = false;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
    private String description;
	
	public SysConnectorServerDto() {
	}
	
	/**
	 * Fill connector server attributes.
	 * 
	 * @param connectorServer original
	 * @since 10.8.0
	 */
	public SysConnectorServerDto(IcConnectorServer connectorServer) {
		this.host = connectorServer.getHost();
		this.password = connectorServer.getPassword();
		this.port = connectorServer.getPort();
		this.useSsl = connectorServer.isUseSsl();
		this.timeout = connectorServer.getTimeout();
	}

	@Override
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	@Override
	public boolean isUseSsl() {
		return useSsl;
	}

	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	@Override
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
	
	/**
	 * Local or remote connector server.
	 * 
	 * @return local (true), remote (false)
	 * @since 10.8.0
	 */
	public boolean isLocal() {
		return local;
	}
	
	/**
	 * Local or remote connector server.
	 * 
	 * @param local local (true), remote (false)
	 * @since 10.8.0
	 */
	public void setLocal(boolean local) {
		this.local = local;
	}

	/**
	 * Server description.
	 * 
	 * @return description
	 * @since 10.8.0
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Server description.
	 * 
	 * @param description custom description
	 * @since 10.8.0
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
