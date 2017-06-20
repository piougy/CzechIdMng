package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for identity
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "identities")
@ApiModel(description = "Identity domain object")
public class IdmIdentityDto extends AbstractDto implements Disableable {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique identity username. Could be used as identifier in rest endpoints")
	private String username;	
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private transient GuardedString password;	
	@Size(max = DefaultFieldLengths.NAME)
	private String firstName;
	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	private String lastName;
	@Email
	@Size(max = DefaultFieldLengths.EMAIL_ADDRESS)
	@ApiModelProperty(notes = "Email", dataType = "email")
	private String email;
	@Size(max = 30)
	@ApiModelProperty(notes = "Phone")
	private String phone;
	@Size(max = 100)
	private String titleBefore;
	@Size(max = 100)
	private String titleAfter;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@NotNull
	private boolean disabled;
	
	public IdmIdentityDto() {
	}
	
	public IdmIdentityDto(UUID id) {
		super(id);
	}
	
	public IdmIdentityDto(String username) {
		this.username = username;
	}
	
	public IdmIdentityDto(Auditable auditable) {
		super(auditable);
	}
	
	public IdmIdentityDto(UUID id, String username) {
		super(id);
		this.username = username;
	}
	
	public IdmIdentityDto(Auditable auditable, String username) {
		super(auditable);
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTitleBefore() {
		return titleBefore;
	}

	public void setTitleBefore(String titleBefore) {
		this.titleBefore = titleBefore;
	}

	public String getTitleAfter() {
		return titleAfter;
	}

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
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
	
	public void setPassword(GuardedString password) {
		this.password = password;
	}
	
	public GuardedString getPassword() {
		return password;
	}
}
