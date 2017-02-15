package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;
import java.text.MessageFormat;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;

/**
 * Define key for provisioning attribute. Basic part is name of schema attribute
 * and type of strategy
 * 
 * @author svandav
 *
 */
public class ProvisioningAttributeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private String schemaAttributeName;
	private AttributeMappingStrategyType strategyType;

	public ProvisioningAttributeDto(String schemaAttributeName, AttributeMappingStrategyType strategyType) {
		super();
		this.schemaAttributeName = schemaAttributeName;
		this.strategyType = strategyType;
	}

	public String getSchemaAttributeName() {
		return schemaAttributeName;
	}

	public void setSchemaAttributeName(String schemaAttributeName) {
		this.schemaAttributeName = schemaAttributeName;
	}

	public AttributeMappingStrategyType getStrategyType() {
		return strategyType;
	}

	public void setStrategyType(AttributeMappingStrategyType strategyType) {
		this.strategyType = strategyType;
	}
	
	public String getKey(){
		return MessageFormat.format("{0}_{1}", schemaAttributeName, strategyType);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((schemaAttributeName == null) ? 0 : schemaAttributeName.hashCode());
		result = prime * result + ((strategyType == null) ? 0 : strategyType.hashCode());
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProvisioningAttributeDto other = (ProvisioningAttributeDto) obj;
		if (schemaAttributeName == null) {
			if (other.schemaAttributeName != null)
				return false;
		} else if (!schemaAttributeName.equals(other.schemaAttributeName)){
			return false;
		}
		if (strategyType != other.strategyType) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0} ({1})", schemaAttributeName, strategyType);
	}
	
	public static ProvisioningAttributeDto createProvisioningAttributeKey(AttributeMapping attribute){
		return new ProvisioningAttributeDto(attribute.getSchemaAttribute().getName(), attribute.getStrategyType()); 
	}

}
