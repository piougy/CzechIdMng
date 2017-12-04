package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Automatic role attribute dto
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.6.0
 *
 */
@Relation(collectionRelation = "automaticRoleAttributes")
public class IdmAutomaticRoleAttributeDto extends AbstractIdmAutomaticRoleDto {

	private static final long serialVersionUID = -4773624183948237920L;

}
