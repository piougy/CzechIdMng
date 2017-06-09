package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

import java.util.UUID;

/**
 * Dto for role composition
 *
 * @author svandav
 */
public class IdmRoleCompositionDto extends AbstractDto {

    private static final long serialVersionUID = 1L;

    @Embedded(dtoClass = IdmRoleDto.class)
    private UUID superior;

    @Embedded(dtoClass = IdmRoleDto.class)
    private UUID sub;

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