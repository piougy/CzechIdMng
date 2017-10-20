package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Default sync configuration DTO
 * 
 * @author svandav
 *
 */

@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;


}
