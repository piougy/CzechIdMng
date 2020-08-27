package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModelProperty;

/**
 * Uniform password definition dto
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Relation(collectionRelation = "uniformPasswords")
public class AccUniformPasswordDto extends AbstractDto implements Codeable, Disableable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String description;
	private boolean disabled;
	@ApiModelProperty(notes = "Change password also in IdM.")
	private boolean changeInIdm;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isChangeInIdm() {
		return changeInIdm;
	}

	public void setChangeInIdm(boolean changeInIdm) {
		this.changeInIdm = changeInIdm;
	}
}
