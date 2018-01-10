package eu.bcvsolutions.idm.acc.dto;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Wrapper class for attribute definition and list of attribute values
 * This object is uses as key in the cache.
 * @author svandav
 *
 */
public class AttributeValueWrapperDto {

	AttributeMapping attribute;
	List<IcAttribute> icAttributes;

	public AttributeValueWrapperDto(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		super();
		this.attribute = attribute;
		this.icAttributes = icAttributes;
	}

	public AttributeMapping getAttribute() {
		return attribute;
	}

	public void setAttribute(AttributeMapping attribute) {
		this.attribute = attribute;
	}

	public List<IcAttribute> getIcAttributes() {
		return icAttributes;
	}

	public void setIcAttributes(List<IcAttribute> icAttributes) {
		this.icAttributes = icAttributes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((icAttributes == null) ? 0 : icAttributes.hashCode());
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
		if (!(obj instanceof AttributeValueWrapperDto)) {
			return false;
		}
		AttributeValueWrapperDto other = (AttributeValueWrapperDto) obj;
		if (attribute == null) {
			if (other.attribute != null) {
				return false;
			}
		} else if (!attribute.equals(other.attribute)) {
			return false;
		}
		if (icAttributes == null) {
			if (other.icAttributes != null) {
				return false;
			}
		} else if (!icAttributes.equals(other.icAttributes)) {
			return false;
		}
		return true;
	}

}
