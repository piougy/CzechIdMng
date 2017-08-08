package eu.bcvsolutions.idm.example.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Example product
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "exampleProducts")
@ApiModel(description = "Example product")
public class ExampleProductDto extends AbstractDto implements Codeable, Disableable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique example product's code. Could be used as identifier in rest endpoints.")
	private String code;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String name;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@ApiModelProperty(notes = "Price can be null - product is for free")
	private BigDecimal price;
	@ApiModelProperty(notes = "Disabled product is not available for odrering.")
	private boolean disabled;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
