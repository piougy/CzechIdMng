package eu.bcvsolutions.idm.acc.entity;

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
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Target system setting - is used for accont management and provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system", indexes = {
		@Index(name = "ux_system_name", columnList = "name", unique = true) })
public class SysSystem extends AbstractEntity {

	private static final long serialVersionUID = -8276147852371288351L;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;
	
	@Audited
	@Column(name = "description")
	private String description;
	
	@Audited
	@NotNull
	@Column(name = "virtual", nullable = false)
	private boolean virtual;
	
	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag
	
	@Audited
	@JsonIgnore
	@OneToMany(mappedBy = "system")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<AccRoleSystem> roleSystems; // only for auditing

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
}
