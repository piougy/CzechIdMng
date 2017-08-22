package eu.bcvsolutions.idm.vs.service.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.vs.domain.VsRequestEventType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for request in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "requests")
@ApiModel(description = "Request in virtual system")
public class VsRequestDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique account identifier. UID on system and for connector.")
	private String uid;
	@ApiModelProperty(required = true, notes = "CzechIdM system identifier. UID on system and for connector.")
	private UUID systemId;
	@ApiModelProperty(required = true, notes = "Connector identifier. UID on system and for connector.")
	private String connectorKey;
	private VsRequestEventType operationType;
	private String objectClass;
	@NotNull
	private VsRequestState state;
	@NotNull
	private boolean executeImmediately;
	@Embedded(dtoClass = VsRequestBatchDto.class)
	private UUID batch;

	// private IcConnectorConfiguration configuration;
	// private List<IcAttribute> attributes;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}

	public VsRequestEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsRequestEventType operationType) {
		this.operationType = operationType;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public UUID getBatch() {
		return batch;
	}

	public void setBatch(UUID batch) {
		this.batch = batch;
	}
}
