package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;

/**
 * Event processor dto
 * 
 * @author Radek Tomiška
 */
public class EntityEventProcessorDto {

	private String id;
	private String name;
	private String module;
	private String description;
	private String entityType;
	private List<String> eventTypes;
	private int order;
	private boolean disabled;
	private boolean disableable;
	private boolean closable;
	private ConfigurationMap configurationProperties;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityType() {
		return entityType;
	}

	public List<String> getEventTypes() {
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

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public ConfigurationMap getConfigurationProperties() {
		return configurationProperties;
	}
	
	public void setConfigurationProperties(ConfigurationMap configurationProperties) {
		this.configurationProperties = configurationProperties;
	}
}
