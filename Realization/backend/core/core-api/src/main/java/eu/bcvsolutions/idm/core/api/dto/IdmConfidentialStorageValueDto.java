package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Confidential Storage Value
 * 
 * @author Patrik Stloukal
 */
@Relation(collectionRelation = "confidentialStorageValues")
public class IdmConfidentialStorageValueDto extends AbstractDto {

	private static final long serialVersionUID = -4324609629971552751L;

	@JsonProperty(access = Access.READ_ONLY)
	private UUID ownerId;

	@JsonProperty(access = Access.READ_ONLY)
	private String ownerType;

	private String key;
	
	@JsonIgnore
	private byte[] value;
	
	private Serializable serializableValue;

	public IdmConfidentialStorageValueDto() {
	}

	public IdmConfidentialStorageValueDto(UUID id) {
		super(id);
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public Serializable getSerializableValue() {
		return serializableValue;
	}

	public void setSerializableValue(Serializable serializableValue) {
		this.serializableValue = serializableValue;
	}
	
	

}
