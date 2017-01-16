package eu.bcvsolutions.idm.acc.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Contains provisioning content
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningContext implements Serializable {

	private static final long serialVersionUID = 778636708338293486L;
	private Map<UUID, Object> accountObject; // account attributes 	
	private IcConnectorObject connectorObject; // provisioning attributes 
	
	public ProvisioningContext() {
	}
	
	public ProvisioningContext(Map<UUID, Object> accountObject) {
		this.accountObject = accountObject;
	}
	
	public ProvisioningContext( Map<UUID, Object> accountObject, IcConnectorObject connectorObject) {
		this(accountObject);
		this.connectorObject = connectorObject;
	}
	
	public ProvisioningContext(IcConnectorObject connectorObject) {
		this(null, connectorObject);
	}
	

	/**
	 * Account attributes
	 * 
	 * @return
	 */
	public Map<UUID, Object> getAccountObject() {
		return accountObject;
	}
	
	public void setAccountObject(Map<UUID, Object> accountObject) {
		this.accountObject = accountObject;
	}
	
	/**
	 * Provisioning context - object type + attributes
	 * 
	 * @return
	 */
	public IcConnectorObject getConnectorObject() {
		return connectorObject;
	}

	public void setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
	}
}
