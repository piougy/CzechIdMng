package eu.bcvsolutions.idm.core.api.domain;

/**
 * Password policy type
 * - VALIDATE: is used only for validate password
 * - GENERATE: is used only for generating password
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public enum IdmPasswordPolicyType {
	VALIDATE, //policy for validating
	GENERATE; // policy for generating password
}
