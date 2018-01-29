package eu.bcvsolutions.idm.core.api.dto;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import org.springframework.hateoas.core.Relation;

/**
 * Event processor dto
 * 
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "entityEventProcessors")
public class EntityEventProcessorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	private String entityType;
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
}
