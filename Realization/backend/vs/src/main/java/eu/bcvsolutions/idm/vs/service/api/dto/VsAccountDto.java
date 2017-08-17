package eu.bcvsolutions.idm.vs.service.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for account in virutal system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "accounts")
@ApiModel(description = "Account in virtual system")
public class VsAccountDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique account identifier. UID on system and for connector.")
	private String uid;
	private boolean enable;
	@ApiModelProperty(required = true, notes = "CzechIdM system identifier. UID on system and for connector.")
	private UUID systemId;
	@ApiModelProperty(required = true, notes = "Connector identifier. UID on system and for connector.")
	private String connectorKey;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
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
}
