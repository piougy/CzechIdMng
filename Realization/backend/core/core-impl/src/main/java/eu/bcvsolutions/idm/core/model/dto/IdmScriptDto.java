package eu.bcvsolutions.idm.core.model.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.model.domain.IdmScriptCategory;

@Relation(collectionRelation = "scripts")
public class IdmScriptDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String script;
	private IdmScriptCategory category = IdmScriptCategory.DEFAULT;
	private String description;
	
	public String getName() {
		return name;
	}
	
	public String getScript() {
		return script;
	}
	
	public IdmScriptCategory getCategory() {
		return category;
	}
	
	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public void setCategory(IdmScriptCategory category) {
		this.category = category;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
