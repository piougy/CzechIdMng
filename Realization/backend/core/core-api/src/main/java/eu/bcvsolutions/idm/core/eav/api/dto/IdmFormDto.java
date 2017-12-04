package eu.bcvsolutions.idm.core.eav.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Persistent common eav form
 * - for LRT, reports etc.
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
@Relation(collectionRelation = "forms")
public class IdmFormDto extends AbstractDto {
	
	private static final long serialVersionUID = 1L;
	//
	private String name; // user friendly name
	@NotNull
	@Embedded(dtoClass = IdmFormDefinitionDto.class)
	private UUID formDefinition;
	private UUID ownerId;
	private String ownerType;
	private String ownerCode; // user friendly owner code - can be changed, look out
	private List<IdmFormValueDto> values;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setFormDefinition(UUID formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public UUID getFormDefinition() {
		return formDefinition;
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getOwnerCode() {
		return ownerCode;
	}

	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}
	
	public List<IdmFormValueDto> getValues() {
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<IdmFormValueDto> values) {
		this.values = values;
	}
}
