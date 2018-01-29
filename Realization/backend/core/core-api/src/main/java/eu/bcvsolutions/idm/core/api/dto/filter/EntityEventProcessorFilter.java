package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.event.EventType;
import java.io.Serializable;
import org.hibernate.type.EntityType;

/**
 * Entity event processors filter
 * 
 * @author Radek Tomiška
 */
public class EntityEventProcessorFilter implements BaseFilter {

	Class<? extends Serializable> contentClass;
	private String entityType;
	private String eventType;
	private String name;
	private String description;
	private String module;

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

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
}
