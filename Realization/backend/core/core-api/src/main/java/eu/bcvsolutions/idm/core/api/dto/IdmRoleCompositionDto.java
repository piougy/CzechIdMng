package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Dto for role composition
 *
 * @author svandav
 */
public class IdmRoleCompositionDto extends AbstractDto {

    private static final long serialVersionUID = 1L;
    
    @NotNull
    @Embedded(dtoClass = IdmRoleDto.class)
    private UUID superior;
    @NotNull
    @Embedded(dtoClass = IdmRoleDto.class)
    private UUID sub;
    
    public IdmRoleCompositionDto() {
	}
    
    public IdmRoleCompositionDto(UUID superiorRole, UUID subRole) {
		this.superior = superiorRole;
		this.sub = subRole;
	}

    public UUID getSuperior() {
        return superior;
    }

    public void setSuperior(UUID superior) {
        this.superior = superior;
    }

    public UUID getSub() {
        return sub;
    }

    public void setSub(UUID sub) {
        this.sub = sub;
    }

}