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

/**
 * 
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "products")
@ApiModel(description = "Example product")
public class ExampleProductDto extends AbstractDto implements Codeable, Disableable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String code;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String name;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	private BigDecimal price;
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
