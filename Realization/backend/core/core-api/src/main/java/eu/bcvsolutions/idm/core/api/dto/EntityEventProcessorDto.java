package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;

/**
 * Event processor dto
 * 
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "entityEventProcessors")
public class EntityEventProcessorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	@JsonProperty(access = Access.READ_ONLY)
	private Class<? extends Serializable> contentClass; // dto class
	private String entityType; // => content simple dto type (refactored ...)
	private List<String> eventTypes;
	private int order;
	private boolean disableable;
	private boolean closable;
	private ConfigurationMap configurationProperties;

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityType() {
		return entityType;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isDisableable() {
		return disableable;
	}

	public void setDisableable(boolean disableable) {
		this.disableable = disableable;
	}

	public boolean isClosable() {
		return closable;
	}

	public void setClosable(boolean closable) {
		this.closable = closable;
	}
	
	public ConfigurationMap getConfigurationProperties() {
		return configurationProperties;
	}
	
	public void setConfigurationProperties(ConfigurationMap configurationProperties) {
		this.configurationProperties = configurationProperties;
	}
	
	public Class<? extends Serializable> getContentClass() {
		return contentClass;
	}
	
	public void setContentClass(Class<? extends Serializable> contentClass) {
		this.contentClass = contentClass;
	}
}
