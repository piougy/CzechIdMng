package eu.bcvsolutions.idm.core.api.dto.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;

/**
 * Entity event processors filter
 * 
 * @author Radek Tomiška
 */
public class EntityEventProcessorFilter extends DataFilter {

	Class<? extends Serializable> contentClass;
	private String entityType; // equals - simple name
	private List<String> eventTypes; // and - processor has to support all
	private String name; // equals
	private String module; // equals
	private String description; // like

	public EntityEventProcessorFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public EntityEventProcessorFilter(MultiValueMap<String, Object> data) {
		super(EntityEventProcessorDto.class, data);
	}
	
	public Class<? extends Serializable> getContentClass() {
		return contentClass;
	}

	public void setContentClass(Class<? extends Serializable> contentClass) {
		this.contentClass = contentClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public List<String> getEventTypes() {
		if (eventTypes == null) {
			eventTypes = new ArrayList<>();
		}
		return eventTypes;
	}
	
	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}
}
