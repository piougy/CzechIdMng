package eu.bcvsolutions.idm.core.api.domain;

/**
 * Comparison for attribute and value. Used in 
 * 
 * @author Ondrej Kopr
 *
 */

public enum AutomaticRoleAttributeRuleComparison {
	EQUALS,
	NOT_EQUALS,
	START_WITH,
	NOT_START_WITH,
	END_WITH,
	NOT_END_WITH,
	IS_EMPTY, // Is empty or is null
	IS_NOT_EMPTY,
	CONTAINS,
	NOT_CONTAINS,
	LESS_THAN_OR_EQUAL,
	GREATER_THAN_OR_EQUAL
}
