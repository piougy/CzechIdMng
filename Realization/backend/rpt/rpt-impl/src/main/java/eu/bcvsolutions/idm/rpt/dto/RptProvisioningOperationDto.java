package eu.bcvsolutions.idm.rpt.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * PReport provisioning operation
 * 
 * @author Radek Tomiška
 *
 */
public class RptProvisioningOperationDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private ProvisioningEventType operationType;
	private String systemEntityType;
	private String systemEntityUid;
	private UUID entityIdentifier;
	private String system;
	private Map<String, String> provisioningValues; // keep value of provisioninq. Key is name of attribute. Value is value attribute.

	public RptProvisioningOperationDto() {
	}
	
	public RptProvisioningOperationDto(Auditable auditable) {
		super(auditable);
	}
	
	public Map<String, String> getProvisioningValues() {
		if (provisioningValues == null) {
			provisioningValues = new LinkedHashMap<>();
		}
		return provisioningValues;
	}

	public void setSystemEntityType(String systemEntityType) {
		this.systemEntityType = systemEntityType;
	}

	public String getSystemEntityType() {
		return systemEntityType;
	}
	
	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}
	
	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public void setProvisioningValues(Map<String, String> provisioningValues) {
		this.provisioningValues = provisioningValues;
	}
}
