package eu.bcvsolutions.idm.core.eav.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModelProperty;

/**
 * Code list ~ form defifinition for code list items
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Relation(collectionRelation = "codeLists")
public class IdmCodeListDto extends AbstractDto implements Codeable, ExternalIdentifiable {
	
	private static final long serialVersionUID = 1L;
	//
	@NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String code; // ~ form dfinition code
    @NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String name; // ~ form definition name
	private IdmFormDefinitionDto formDefinition;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
    private String description;
	
	public IdmCodeListDto() {
	}
	
	public IdmCodeListDto(UUID id) {
		super(id);
	}
	
	public IdmCodeListDto(Auditable auditable) {
		super(auditable);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
	
	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
