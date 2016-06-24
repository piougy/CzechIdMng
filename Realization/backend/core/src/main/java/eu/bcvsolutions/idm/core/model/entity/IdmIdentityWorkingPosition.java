package eu.bcvsolutions.idm.core.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "idm_identity_working_position")
public class IdmIdentityWorkingPosition extends AbstractEntity implements ValidableEntity {

	private static final long serialVersionUID = 328041550861866181L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id")
	private IdmIdentity identity;
	
	@Column(name = "valid_from")
	@Temporal(TemporalType.DATE)
	private Date validFrom;
	
	@Column(name = "valid_till")
	@Temporal(TemporalType.DATE)
	private Date validTill;
	
	@Column(name = "position")
	private String position; // TODO: will be codelist
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "manager_id", referencedColumnName = "id")
	private IdmIdentity manager;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "organization_id", referencedColumnName = "id")
	private IdmOrganization organization;
	
	
	public IdmIdentityWorkingPosition() {
	}
	
	public IdmIdentityWorkingPosition(Long id) {
		super(id);
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTo) {
		this.validTill = validTo;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public IdmIdentity getManager() {
		return manager;
	}

	public void setManager(IdmIdentity manager) {
		this.manager = manager;
	}

	public IdmOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(IdmOrganization organization) {
		this.organization = organization;
	}
}
