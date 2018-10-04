package eu.bcvsolutions.idm.core.api.domain;

/**
 * Enum for generate password policies.
 * - RANDOM 
 * - PASSPHRASE
 * - PREFIX_AND_SUFFIX
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public enum IdmPasswordPolicyGenerateType {
	RANDOM,
	PASSPHRASE,
	PREFIX_AND_SUFFIX;
}
