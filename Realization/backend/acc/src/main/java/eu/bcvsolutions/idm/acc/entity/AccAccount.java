package eu.bcvsolutions.idm.acc.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Account on target system
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "acc_account", indexes = { 
		@Index(name = "ux_acc_account_sys_entity", columnList = "system_entity_id", unique = true),
		@Index(name = "ux_account_uid", columnList = "uid,system_id", unique = true),
		@Index(name = "idx_acc_account_sys_id", columnList = "system_id"),
		@Index(name = "idx_acc_account_sys_entity", columnList = "system_entity_id")
		})
public class AccAccount extends AbstractEntity {
	
	private static final long serialVersionUID = -565558977675057360L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "uid", length = DefaultFieldLengths.UID, nullable = false)
	private String uid;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type")
	private SystemEntityType entityType;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false)
	private AccountType accountType;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystem system;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "system_entity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystemEntity systemEntity;
	
	@JsonIgnore
	@OneToMany(mappedBy = "account")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<AccIdentityAccount> identityAccounts;  // only for hibernate mappnig - we dont want lazy lists
	
	@Audited
	@Column(name = "in_protection", nullable = true)
	private boolean inProtection = false;
	
	@Audited
	@Column(name = "end_of_protection", nullable = true)
	private DateTime endOfProtection;

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
	
	public AccountType getAccountType() {
		return accountType;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public SysSystemEntity getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(SysSystemEntity systemEntity) {
		this.systemEntity = systemEntity;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUid() {
		return uid;
	}
	
	public boolean isInProtection() {
		return inProtection;
	}

	public void setInProtection(boolean inProtection) {
		this.inProtection = inProtection;
	}

	public DateTime getEndOfProtection() {
		return endOfProtection;
	}

	public void setEndOfProtection(DateTime endOfProtection) {
		this.endOfProtection = endOfProtection;
	}

	/**
	 * Return real uid from system entity.
	 * If system entity do not exist, then return uid from account.
	 * 
	 * @return
	 */
	public String getRealUid() {
		if (systemEntity != null) {
			return systemEntity.getUid();
		}
		return uid;
	}
	
	/**
	 * Check if account is in protection. Validate end of protection too.
	 * 
	 * @param account
	 * @return
	 */
	public boolean isAccountProtectedAndValid() {
		if (this.isInProtection()) {
			if (this.getEndOfProtection() == null) {
				return true;
			}
			if (this.getEndOfProtection() != null && this.getEndOfProtection().isAfterNow()) {
				return true;
			}
		}
		return false;
	}
	
	public SystemEntityType getEntityType() {
		return entityType;
	}
	
	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}
}
