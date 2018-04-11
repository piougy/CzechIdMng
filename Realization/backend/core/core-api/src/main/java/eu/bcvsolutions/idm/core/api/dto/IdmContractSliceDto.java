package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Contract time slice DTO
 *
 * @author Svanda
 */
@Relation(collectionRelation = "contractSlices")
public class IdmContractSliceDto extends IdmIdentityContractDto implements ValidableEntity {

	private static final long serialVersionUID = 1L;

}
