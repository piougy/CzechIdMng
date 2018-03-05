package eu.bcvsolutions.idm.core.api.domain;

/**
 * Enum that define types of attributes in automatic role attribute rule definition
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public enum AutomaticRoleAttributeRuleType {
	IDENTITY, // attribute will be in basic identity attributes (username, description, telephone, ...)
	IDENTITY_EAV, // attribute will be in identity eav attributes
	CONTRACT,  // attribute will be in basic contract attributes
	CONTRACT_EAV  // attribute will be in contract eav attributes (workPosition, position, externe)
}
