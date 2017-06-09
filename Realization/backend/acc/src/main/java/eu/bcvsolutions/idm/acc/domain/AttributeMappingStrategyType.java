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
	WRITE_IF_NULL

}
