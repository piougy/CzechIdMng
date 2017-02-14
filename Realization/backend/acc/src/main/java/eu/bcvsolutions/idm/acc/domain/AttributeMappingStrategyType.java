package eu.bcvsolutions.idm.acc.domain;

/**
 * Type of strategy for attribute mapping 
 * @author svandav
 *
 */
public enum AttributeMappingStrategyType {

	SET, 
	MERGE,
	AUTHORITATIVE_MERGE,
	CREATE,
	IF_NULL

}
