package eu.bcvsolutions.idm.acc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;


/**
 * Connector DTO extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Relation(collectionRelation = "connectorTypes")
public class ConnectorTypeDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	private String iconKey;
	private String connectorName;
	private String wizardStepName;
	private Map<String, String> metadata;
	@JsonProperty(value = "_embedded", access = Access.READ_ONLY)
	@ApiModelProperty(accessMode = AccessMode.READ_ONLY)
	private Map<String, BaseDto> embedded;
	// Version of current found connector.
	private String version;
	// I current found connector local?
	private boolean local = true;
	private boolean hideParentConnector;
	// Defines if that wizard is opened from existed system.
	private boolean reopened = false;
	private int order;

	public String getIconKey() {
		return iconKey;
	}

	public void setIconKey(String iconKey) {
		this.iconKey = iconKey;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public Map<String, String> getMetadata() {
		if (metadata == null) {
			metadata = new HashMap<>();
		}
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getWizardStepName() {
		return wizardStepName;
	}

	public void setWizardStepName(String wizardStepName) {
		this.wizardStepName = wizardStepName;
	}

	public Map<String, BaseDto> getEmbedded() {
		if(embedded == null){
			embedded = new HashMap<>();
		}
		return embedded;
	}

	public void setEmbedded(Map<String, BaseDto> embedded) {
		this.embedded = embedded;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public boolean isHideParentConnector() {
		return hideParentConnector;
	}

	public void setHideParentConnector(boolean hideParentConnector) {
		this.hideParentConnector = hideParentConnector;
	}

	public boolean isReopened() {
		return reopened;
	}

	public void setReopened(boolean reopened) {
		this.reopened = reopened;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
