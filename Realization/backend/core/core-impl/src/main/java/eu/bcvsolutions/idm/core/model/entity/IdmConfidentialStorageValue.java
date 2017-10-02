package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * 
 * "Naive" confidential storage
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_confidential_storage", indexes = { 
		@Index(name = "idx_confidential_storage_o_i", columnList = "owner_id"),
		@Index(name = "idx_confidential_storage_o_t", columnList = "owner_type"),
		@Index(name = "idx_confidential_storage_key", columnList = "storage_key")
})
public class IdmConfidentialStorageValue extends AbstractEntity {

	private static final long serialVersionUID = 1899675671390881273L;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@Audited
	@NotNull
	@Column(name = "owner_id", nullable = false, length = 16)
	private UUID ownerId;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "storage_key", length = DefaultFieldLengths.NAME, nullable = false)
	private String key;
	
	@Column(name = "storage_value", length = Integer.MAX_VALUE - 1)
	private byte[] value;

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
		return value == null ? null : value.clone();
	}

	public void setValue(byte[] value) {
		this.value = value == null ? null : value.clone();
	}
}
