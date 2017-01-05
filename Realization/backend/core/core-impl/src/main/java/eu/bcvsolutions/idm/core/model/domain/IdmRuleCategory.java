package eu.bcvsolutions.idm.core.model.domain;

/**
 * Default category for rule
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public enum IdmRuleCategory {
	DEFAULT, // default rule
	TRANSFORM_FROM, // rule for transform from system
	TRANSFORM_TO, // rule for transform to system
	SYSTEM; // system rule
}
