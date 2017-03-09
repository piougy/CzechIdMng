package eu.bcvsolutions.idm.acc.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;

/**
 * Target system setting - is used for accont management and provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system", indexes = {
		@Index(name = "ux_system_name", columnList = "name", unique = true),
		@Index(name = "idx_idm_password_pol_gen", columnList = "password_pol_val_id"),
		@Index(name = "idx_idm_password_pol_val", columnList = "password_pol_gen_id")})
public class SysSystem extends AbstractEntity implements IdentifiableByName, FormableEntity {

	private static final long serialVersionUID = -8276147852371288351L;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Audited
	@NotNull
	@Column(name = "readonly", nullable = false)
	private boolean readonly;
	
	@Audited
	@NotNull
	@Column(name = "queue", nullable = false)
	private boolean queue;
	
	@Audited
	@NotNull
	@Column(name = "virtual", nullable = false)
	private boolean virtual;
	
	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag
	
	@JsonIgnore
	@OneToMany(mappedBy = "system")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<SysRoleSystem> roleSystems; // only for hibernate mappnig - we dont want lazy lists
	
	@Audited
	@Embedded
	private SysConnectorKey connectorKey;
	
	@Audited
	@Column(name = "remote", nullable = false)
	private boolean remote;
	
	@Audited
	@Embedded
	private SysConnectorServer connectorServer;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "password_pol_val_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmPasswordPolicy passwordPolicyValidate;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "password_pol_gen_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmPasswordPolicy passwordPolicyGenerate;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdmPasswordPolicy getPasswordPolicyValidate() {
		return passwordPolicyValidate;
	}

	public void setPasswordPolicyValidate(IdmPasswordPolicy passwordPolicyValidate) {
		this.passwordPolicyValidate = passwordPolicyValidate;
	}

	public IdmPasswordPolicy getPasswordPolicyGenerate() {
		return passwordPolicyGenerate;
	}

	public void setPasswordPolicyGenerate(IdmPasswordPolicy passwordPolicyGenerate) {
		this.passwordPolicyGenerate = passwordPolicyGenerate;
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
	
	/**
	 * Configured connector
	 * 
	 * @return
	 */
	public SysConnectorKey getConnectorKey() {
		return connectorKey;
	}
	
	public void setConnectorKey(SysConnectorKey connectorKey) {
		this.connectorKey = connectorKey;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isQueue() {
		return queue;
	}

	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	public boolean isRemote() {
		return remote;
	}

	public SysConnectorServer getConnectorServer() {
		return connectorServer;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public void setConnectorServer(SysConnectorServer connectorServer) {
		this.connectorServer = connectorServer;
	}
	
	@JsonIgnore
	public IcConnectorInstance getConnectorInstance() {
		return new IcConnectorInstanceImpl(this.getConnectorServer(), this.getConnectorKey(), this.isRemote());
	}
}
