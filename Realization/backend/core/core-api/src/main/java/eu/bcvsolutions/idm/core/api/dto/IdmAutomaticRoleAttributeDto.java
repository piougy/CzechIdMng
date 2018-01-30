package eu.bcvsolutions.idm.core.api.dto;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

/**
 * Automatic role attribute dto
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.7.0
 *
 */
@Relation(collectionRelation = "automaticRoleAttributes")
public class IdmAutomaticRoleAttributeDto extends AbstractIdmAutomaticRoleDto {

	private static final long serialVersionUID = -4773624183948237920L;

	@NotNull
	private boolean concept;

	public boolean isConcept() {
		return concept;
	}

	public void setConcept(boolean concept) {
		this.concept = concept;
	}
	
}
