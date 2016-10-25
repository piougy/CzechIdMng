package eu.bcvsolutions.idm.core.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Identity
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_identity", indexes = {
		@Index(name = "ux_idm_identity_username", columnList = "username", unique = true) })
public class IdmIdentity extends AbstractEntity implements IdentifiableByName {

	private static final long serialVersionUID = -3387957881104260630L;
	//
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "username", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String username;

	@JsonProperty(access = Access.WRITE_ONLY)
	@Size(max = DefaultFieldLengths.PASSWORD)
	@Column(name = "password", length = DefaultFieldLengths.PASSWORD)
	private byte[] password;

	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

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
	@Column(name = "description")
	private String description;
	
	@Audited
	@JsonIgnore
	@OneToMany(mappedBy = "identity")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<IdmIdentityRole> roles;
	
	@Audited
	@JsonIgnore
	@OneToMany(mappedBy = "identity")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<IdmIdentityContract> contracts;

	public String getUsername() {
		return username;
	}

	@Override
	@JsonIgnore
	public String getName() {
		return getUsername();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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

	public List<IdmIdentityRole> getRoles() {
		if (roles == null) {
			roles = new ArrayList<>();
		}
		return roles;
	}

	public void setRoles(List<IdmIdentityRole> roles) {
		this.roles = roles;
	}
	
	public List<IdmIdentityContract> getContracts() {
		return contracts;
	}
	
	public void setContracts(List<IdmIdentityContract> contracts) {
		this.contracts = contracts;
	}
}
