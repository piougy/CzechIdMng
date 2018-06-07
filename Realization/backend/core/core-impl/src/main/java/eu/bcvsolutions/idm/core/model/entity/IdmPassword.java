package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Entity that store identities passwords in hash.
 * Only password isn't audited.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Entity
@Table(name = "idm_password", indexes = {
		@Index(name = "ux_idm_password_identity", columnList = "identity_id", unique = true)
})
public class IdmPassword extends AbstractEntity implements ValidableEntity, AuditSearchable {

	private static final long serialVersionUID = -8101492061266251152L;
	
	@Column(name = "password")
	private String password;
	
	@Audited
	@OneToOne(optional = false)
	@JoinColumn(name = "identity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity identity;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "must_change")
	private boolean mustChange = false;

	@Audited
	@Column(name = "last_successful_login")
	private DateTime lastSuccessfulLogin;

	@Audited
	@Column(name = "unsuccessful_attempts", nullable = false)
	private int unsuccessfulAttempts;

	@Audited
	@Column(name = "block_login_date")
	private DateTime blockLoginDate;
	
	public DateTime getBlockLoginDate() {
		return blockLoginDate;
	}

	public void setBlockLoginDate(DateTime blockLoginDate) {
		this.blockLoginDate = blockLoginDate;
	}

	public DateTime getLastSuccessfulLogin() {
		return lastSuccessfulLogin;
	}

	public void setLastSuccessfulLogin(DateTime lastSuccessfulLogin) {
		this.lastSuccessfulLogin = lastSuccessfulLogin;
	}

	public int getUnsuccessfulAttempts() {
		return unsuccessfulAttempts;
	}

	public void setUnsuccessfulAttempts(int unsuccessfulAttempts) {
		this.unsuccessfulAttempts = unsuccessfulAttempts;
	}

	public IdmPassword() {
		// Auto-generated constructor
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}
	
	@Override
	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public boolean isMustChange() {
		return mustChange;
	}

	public void setMustChange(boolean mustChange) {
		this.mustChange = mustChange;
	}

	@Override
	public String getOwnerId() {
		return identity.getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return identity.getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return null;
	}

	@Override
	public String getSubOwnerType() {
		return IdmPassword.class.getName();
	}
}
