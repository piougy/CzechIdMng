package eu.bcvsolutions.idm.acc.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;

/**
 * Account on target system
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "acc_account", indexes = { 
		@Index(name = "ux_account_system_entity", columnList = "system_entity_id", unique = true),
		@Index(name = "ux_account_uid", columnList = "uid,system_id", unique = true) 
		})
public class AccAccount extends AbstractEntity {
	
	private static final long serialVersionUID = -565558977675057360L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "uid", length = DefaultFieldLengths.UID, nullable = false)
	private String uid;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false)
	private AccountType accountType;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id")
	private SysSystem system;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "system_entity_id", referencedColumnName = "id")
	private SysSystemEntity systemEntity;
	
	@Audited
	@JsonIgnore
	@OneToMany(mappedBy = "account")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<AccIdentityAccount> identityAccounts;

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
	
	public void setIdentityAccounts(List<AccIdentityAccount> identityAccounts) {
		this.identityAccounts = identityAccounts;
	}
	
	public List<AccIdentityAccount> getIdentityAccounts() {
		return identityAccounts;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUid() {
		return uid;
	}
}
