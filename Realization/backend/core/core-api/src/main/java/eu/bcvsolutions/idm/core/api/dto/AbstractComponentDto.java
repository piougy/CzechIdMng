package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Bean representation. Used for registrable, configurable application components (module descriptors, processors ...) 
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractComponentDto implements BaseDto {
	
	private static final long serialVersionUID = 1L;
	
	@JsonDeserialize(as = String.class)
	private String id; // bean name / identifier (spring bean name or other identifier)
	private String name; // component name - given name e.g. save-identity-processor 
	private String module; // component module
	private String description; // component description
	private boolean disabled; // component is disabled
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id = id == null ? null : id.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
	
	public String getModule() {
		return module;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
