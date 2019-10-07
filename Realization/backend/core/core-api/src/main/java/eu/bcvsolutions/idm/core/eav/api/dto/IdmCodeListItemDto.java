package eu.bcvsolutions.idm.core.eav.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Code lists 
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Relation(collectionRelation = "codeListItems")
public class IdmCodeListItemDto extends FormableDto implements ExternalIdentifiable {
	
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	@Embedded(dtoClass = IdmCodeListDto.class)
	private UUID codeList;
	@NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String code;
    @NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String name;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
    private String description;
	@ApiModelProperty(notes = "Items level - decorator only (label with color).")
	private NotificationLevel level;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Items icon - decorator only.")
	private String icon;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public UUID getCodeList() {
		return codeList;
	}
	
	public void setCodeList(UUID codeList) {
		this.codeList = codeList;
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
    
    public NotificationLevel getLevel() {
		return level;
	}
    
    public void setLevel(NotificationLevel level) {
		this.level = level;
	}
    
    public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getIcon() {
		return icon;
	}
}
