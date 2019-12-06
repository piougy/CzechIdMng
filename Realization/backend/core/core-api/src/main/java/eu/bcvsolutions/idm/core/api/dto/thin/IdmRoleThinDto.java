package eu.bcvsolutions.idm.core.api.dto.thin;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * DTO for role - thin variant.
 * - purpose - disable embedded annotations only.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Relation(collectionRelation = "roles")
public class IdmRoleThinDto extends IdmRoleDto {

    private static final long serialVersionUID = 1L;
    //
    @Embedded(enabled= false, dtoClass = IdmRequestItemDto.class)
    private UUID requestItem; // Isn't persist in the entity
    @Embedded(enabled= false, dtoClass = IdmFormDefinitionDto.class)
	private UUID identityRoleAttributeDefinition;
    
	public UUID getRequestItem() {
		return requestItem;
	}
	
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}
	
	public UUID getIdentityRoleAttributeDefinition() {
		return identityRoleAttributeDefinition;
	}
	
	public void setIdentityRoleAttributeDefinition(UUID identityRoleAttributeDefinition) {
		this.identityRoleAttributeDefinition = identityRoleAttributeDefinition;
	}
}