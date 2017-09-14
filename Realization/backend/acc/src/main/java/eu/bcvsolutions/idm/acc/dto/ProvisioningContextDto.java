package eu.bcvsolutions.idm.acc.dto;

import java.util.Map;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * DTO for {@link ProvisioningContext}, is necessary to have DTO also for
 * {@link ProvisioningContext}, because {@link ProvisioningContext} is part of
 * {@link SysProvisioningOperationDto} and {@link SysProvisioningOperation} and
 * every persist save this content
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class ProvisioningContextDto {
	
	private Map<ProvisioningAttributeDto, Object> accountObject; // account attributes 	
	private IcConnectorObject connectorObject; // provisioning attributes 
	
	public ProvisioningContextDto() {
	}
	
	public ProvisioningContextDto(Map<ProvisioningAttributeDto, Object> accountObject) {
		this.accountObject = accountObject;
	}
	
	public ProvisioningContextDto(Map<ProvisioningAttributeDto, Object> accountObject, IcConnectorObject connectorObject) {
		this(accountObject);
		this.connectorObject = connectorObject;
	}
	
	public ProvisioningContextDto(IcConnectorObject connectorObject) {
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
}
