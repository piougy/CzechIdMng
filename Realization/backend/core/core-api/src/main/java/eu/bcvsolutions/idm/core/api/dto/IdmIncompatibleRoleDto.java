package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Segregation of Duties
 * 
 * @author Radek Tomiška 
 * @since 9.4.0
 */
@Relation(collectionRelation = "incompatibleRoles")
public class IdmIncompatibleRoleDto extends AbstractDto implements ExternalIdentifiable, Requestable {

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
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity
    
    public IdmIncompatibleRoleDto() {
	}
    
    public IdmIncompatibleRoleDto(UUID superiorRole, UUID subRole) {
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
	
	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}
}