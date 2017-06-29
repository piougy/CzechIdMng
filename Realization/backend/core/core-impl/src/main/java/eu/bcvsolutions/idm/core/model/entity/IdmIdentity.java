package eu.bcvsolutions.idm.core.model.entity;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

/**
 * Identity
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_identity", indexes = {
		@Index(name = "ux_idm_identity_username", columnList = "username", unique = true) })
public class IdmIdentity extends AbstractEntity implements Codeable, FormableEntity, Disableable, AuditSearchable {

	private static final long serialVersionUID = -3387957881104260630L;
	//
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "username", length = DefaultFieldLengths.NAME, nullable = false)
	private String username;

	@Transient // passwords are saved to confidental storage
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private transient GuardedString password;

	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "first_name", length = DefaultFieldLengths.NAME)
	private String firstName;

	@Audited
	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "last_name", length = DefaultFieldLengths.NAME)
	private String lastName;

	@Audited
	@Email
	@Size(max = DefaultFieldLengths.EMAIL_ADDRESS)
	@Column(name = "email", length = DefaultFieldLengths.EMAIL_ADDRESS)
	private String email;

	@Audited
	@Size(max = 30)
	@Column(name = "phone", length = 30)
	private String phone;

	@Audited
	@Size(max = 100)
	@Column(name = "title_before", length = 100)
	private String titleBefore;

	@Audited
	@Size(max = 100)
	@Column(name = "title_after", length = 100)
	private String titleAfter;

	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@JsonIgnore
	@OneToMany(mappedBy = "identity")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<IdmIdentityContract> contracts; // only for hibernate mappnig - we dont want lazy lists
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;
	
	public IdmIdentity() {
	}
	
	public IdmIdentity(UUID id) {
		super(id);
	}

	public String getUsername() {
		return username;
	}

	@Override
	@JsonIgnore
	public String getCode() {
		return getUsername();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
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
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public String getOwnerId() {
		return this.getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return this.getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return null;
	}

	@Override
	public String getSubOwnerCode() {
		return null;
	}

	@Override
	public String getSubOwnerType() {
		return null;
	}
}
