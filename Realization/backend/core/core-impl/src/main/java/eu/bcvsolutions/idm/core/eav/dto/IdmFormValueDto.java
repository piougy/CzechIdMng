package eu.bcvsolutions.idm.core.eav.dto;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Form instance values
 * 
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "formValues")
public class IdmFormValueDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private UUID ownerId;
	@NotEmpty
	private String ownerType;
	@NotNull
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	@NotNull
	@JsonProperty(access = Access.READ_ONLY)
	private PersistentType persistentType;
	private boolean confidential;
	private String stringValue;
	private Boolean booleanValue;
	private Long longValue;
	private BigDecimal doubleValue;
	private DateTime dateValue;
	private byte[] byteValue;
	@Max(99999)
	private short seq;
	
	public IdmFormValueDto() {
	}
	
	public IdmFormValueDto(UUID id) {
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

	public UUID getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(UUID formAttribute) {
		this.formAttribute = formAttribute;
	}

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public BigDecimal getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(BigDecimal doubleValue) {
		this.doubleValue = doubleValue;
	}

	public DateTime getDateValue() {
		return dateValue;
	}

	public void setDateValue(DateTime dateValue) {
		this.dateValue = dateValue;
	}

	public byte[] getByteValue() {
		return byteValue;
	}

	public void setByteValue(byte[] byteValue) {
		this.byteValue = byteValue;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}
}
