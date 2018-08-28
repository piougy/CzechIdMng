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
	@Column(name = "NAME", length = DefaultFieldLengths.NAME)
	private String name;

	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "STRING_VALUE", nullable = true)
	private String stringValue;

	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "SHORT_TEXT_VALUE", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String shortTextValue;

	@Column(name = "BOOLEAN_VALUE", nullable = true)
	private Boolean booleanValue;

	@Column(name = "LONG_VALUE", nullable = true)
	private Long longValue;
	
	@Column(name = "INT_VALUE", nullable = true)
	private Integer intValue;

	@Column(name = "DOUBLE_VALUE", nullable = true, precision = 38, scale = 4)
	private BigDecimal doubleValue;

	@Column(name = "DATE_VALUE")
	private DateTime dateValue;

	@Column(name = "BYTE_VALUE")
	private byte[] byteValue;

	@JsonDeserialize(as = UUID.class)
	@Column(name = "UUID_VALUE", length = 16)
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
