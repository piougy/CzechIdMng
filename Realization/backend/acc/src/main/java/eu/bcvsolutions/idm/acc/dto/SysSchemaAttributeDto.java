package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysSchemaAttribute}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "schemaAttributes")
public class SysSchemaAttributeDto extends AbstractDto {

	private static final long serialVersionUID = 324117386631561738L;

	private String name;
	@Embedded(dtoClass = SysSchemaObjectClassDto.class)
	private UUID objectClass;
	private String classType;
	private String nativeName;
	private boolean required = false;
	private boolean multivalued = false;
	private boolean createable = false;
	private boolean updateable = false;
	private boolean readable = false;
	private boolean returnedByDefault = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(UUID objectClass) {
		this.objectClass = objectClass;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isMultivalued() {
		return multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	public boolean isCreateable() {
		return createable;
	}

	public void setCreateable(boolean createable) {
		this.createable = createable;
	}

	public boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isReturnedByDefault() {
		return returnedByDefault;
	}

	public void setReturnedByDefault(boolean returnedByDefault) {
		this.returnedByDefault = returnedByDefault;
	}

}
