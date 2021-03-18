package eu.bcvsolutions.idm.acc.domain;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Contains provisioning content
 * 
 * @author Radek Tomiška
 * @author Ondrej Husnik
 *
 */
public class ProvisioningContext implements Serializable {

	private static final long serialVersionUID = 778636708338293486L;
	private Map<ProvisioningAttributeDto, Object> accountObject; // account attributes 	
	private IcConnectorObject connectorObject; // provisioning attributes
	private IcConnectorObject systemConnectorObject; // attributes on system 
	
	public ProvisioningContext() {
	}
	
	public ProvisioningContext(Map<ProvisioningAttributeDto, Object> accountObject) {
		this.accountObject = accountObject;
	}
	
	public ProvisioningContext(Map<ProvisioningAttributeDto, Object> accountObject, IcConnectorObject connectorObject) {
		this(accountObject);
		this.connectorObject = connectorObject;
	}
	
	public ProvisioningContext(ProvisioningContext context) {
		this(context.getAccountObject(), context.getConnectorObject());
		this.systemConnectorObject = context.getSystemConnectorObject();
	}
	
	public ProvisioningContext(IcConnectorObject connectorObject) {
		this(null, connectorObject);
	}
	

	/**
	 * Account attributes
	 * 
	 * @return
	 */
	public Map<ProvisioningAttributeDto, Object> getAccountObject() {
		return accountObject;
	}
	
	public void setAccountObject(Map<ProvisioningAttributeDto, Object> accountObject) {
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

	/**
	 * Values of attributes on the system
	 * 
	 * @return
	 */
	public IcConnectorObject getSystemConnectorObject() {
		return systemConnectorObject;
	}

	public void setSystemConnectorObject(IcConnectorObject systemConnectorObject) {
		this.systemConnectorObject = systemConnectorObject;
	}	
}
