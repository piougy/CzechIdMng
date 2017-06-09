package eu.bcvsolutions.idm.core.api.domain;

/**
 * Default category for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public enum IdmScriptCategory { // VS: TODO: Delete this enumeration (we need module independent solution ... may be String will be solution)
	DEFAULT, // default script
	TRANSFORM_FROM, // script for transform from system 
	TRANSFORM_TO, // script for transform to system
	SYSTEM; // system script
}
