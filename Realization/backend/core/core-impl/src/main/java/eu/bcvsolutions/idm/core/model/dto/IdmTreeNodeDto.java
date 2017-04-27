package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Tree node
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "treeNodes")
public class IdmTreeNodeDto extends AbstractDto implements Disableable, IdentifiableByName {

	private static final long serialVersionUID = 1337282508070610164L;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String code;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String name;
	@Embedded(dtoClass = IdmTreeNodeDto.class, enabled = false)
	private UUID parent;
	@NotNull
	@Embedded(dtoClass = IdmTreeTypeDto.class)
	private UUID treeType;
	private boolean disabled;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getParent() {
		return parent;
	}

	public void setParent(UUID parent) {
		this.parent = parent;
	}

	public UUID getTreeType() {
		return treeType;
	}

	public void setTreeType(UUID treeType) {
		this.treeType = treeType;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
