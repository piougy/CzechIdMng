package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for role composition
 *
 * @author svandav
 */
@Relation(collectionRelation = "roleCompositions")
public class IdmRoleCompositionDto extends AbstractDto implements ExternalIdentifiable {

    private static final long serialVersionUID = 1L;
    
    @Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
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
    
    @Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}