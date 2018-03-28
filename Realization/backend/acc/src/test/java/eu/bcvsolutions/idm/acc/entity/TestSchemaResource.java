package eu.bcvsolutions.idm.acc.entity;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Test schema resource for check if schema and eav attribute will be generate
 * correctly
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = TestSchemaResource.TABLE_NAME)
public class TestSchemaResource {

	public static final String TABLE_NAME = "test_schema_resource";

	@Id
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;

	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "string_value", nullable = true)
	private String stringValue;

	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "short_text_value", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String shortTextValue;

	@Column(name = "boolean_value", nullable = true)
	private Boolean booleanValue;

	@Column(name = "long_value", nullable = true)
	private Long longValue;
	
	@Column(name = "int_value", nullable = true)
	private Integer intValue;

	@Column(name = "double_value", nullable = true, precision = 38, scale = 4)
	private BigDecimal doubleValue;

	@Column(name = "date_value")
	private DateTime dateValue;

	@Column(name = "byte_value")
	private byte[] byteValue;

	@JsonDeserialize(as = UUID.class)
	@Column(name = "uuid_value", length = 16)
	private UUID uuidValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getShortTextValue() {
		return shortTextValue;
	}

	public void setShortTextValue(String shortTextValue) {
		this.shortTextValue = shortTextValue;
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

	public UUID getUuidValue() {
		return uuidValue;
	}

	public void setUuidValue(UUID uuidValue) {
		this.uuidValue = uuidValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

}
