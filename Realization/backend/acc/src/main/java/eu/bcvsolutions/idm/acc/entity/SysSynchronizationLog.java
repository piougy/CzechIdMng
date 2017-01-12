package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.joda.time.LocalDateTime;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSynchronizationLog</i> is responsible for keep log informations about
 * synchronization.
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_log")
public class SysSynchronizationLog extends AbstractEntity {

	private static final long serialVersionUID = -5447620157233410338L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "synchronization_config_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSynchronizationConfig synchronizationConfig;

	@Audited
	@NotNull
	@Column(name = "running", nullable = false)
	private boolean running = false;

	@Audited
	@Column(name = "started")
	private LocalDateTime started;

	@Audited
	@Column(name = "ended")
	private LocalDateTime ended;

	@Audited
	@Lob
	@Column(name = "token")
	private String token;

	@Audited
	@NotNull
	@Column(name = "exceptions_create_entity", nullable = false)
	private int exceptionCreateEntity = 0;

	@Audited
	@NotNull
	@Column(name = "success_create_entity", nullable = false)
	private int successCreateEntity = 0;

	@Audited
	@NotNull
	@Column(name = "exceptions_update_entity", nullable = false)
	private int exceptionUpdateEntity = 0;

	@Audited
	@NotNull
	@Column(name = "success_update_entity", nullable = false)
	private int successUpdateEntity = 0;

	@Audited
	@NotNull
	@Column(name = "success_delete_entity", nullable = false)
	private int successDeleteEntity = 0;

	@Audited
	@NotNull
	@Column(name = "exceptions_delete_entity", nullable = false)
	private int exceptionDeleteEntity = 0;

	@Audited
	@NotNull
	@Column(name = "exceptions_create_account", nullable = false)
	private int exceptionCreateAccount = 0;

	@Audited
	@NotNull
	@Column(name = "success_create_account", nullable = false)
	private int successCreateAccount = 0;

	@Audited
	@NotNull
	@Column(name = "exceptions_update_account", nullable = false)
	private int exceptionUpdateAccount = 0;

	@Audited
	@NotNull
	@Column(name = "success_update_account", nullable = false)
	private int successUpdateAccount = 0;

	@Audited
	@NotNull
	@Column(name = "exceptions_delete_account", nullable = false)
	private int exceptionDeleteAccount = 0;

	@Audited
	@NotNull
	@Column(name = "success_delete_account", nullable = false)
	private int successDeleteAccount = 0;

	public SysSynchronizationConfig getSynchronizationConfig() {
		return synchronizationConfig;
	}

	public void setSynchronizationConfig(SysSynchronizationConfig synchronizationConfig) {
		this.synchronizationConfig = synchronizationConfig;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setStarted(LocalDateTime started) {
		this.started = started;
	}

	public LocalDateTime getEnded() {
		return ended;
	}

	public void setEnded(LocalDateTime ended) {
		this.ended = ended;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getExceptionCreateEntity() {
		return exceptionCreateEntity;
	}

	public void setExceptionCreateEntity(int exceptionCreateEntity) {
		this.exceptionCreateEntity = exceptionCreateEntity;
	}

	public int getSuccessCreateEntity() {
		return successCreateEntity;
	}

	public void setSuccessCreateEntity(int successCreateEntity) {
		this.successCreateEntity = successCreateEntity;
	}

	public int getExceptionUpdateEntity() {
		return exceptionUpdateEntity;
	}

	public void setExceptionUpdateEntity(int exceptionUpdateEntity) {
		this.exceptionUpdateEntity = exceptionUpdateEntity;
	}

	public int getSuccessUpdateEntity() {
		return successUpdateEntity;
	}

	public void setSuccessUpdateEntity(int successUpdateEntity) {
		this.successUpdateEntity = successUpdateEntity;
	}

	public int getSuccessDeleteEntity() {
		return successDeleteEntity;
	}

	public void setSuccessDeleteEntity(int successDeleteEntity) {
		this.successDeleteEntity = successDeleteEntity;
	}

	public int getExceptionDeleteEntity() {
		return exceptionDeleteEntity;
	}

	public void setExceptionDeleteEntity(int exceptionDeleteEntity) {
		this.exceptionDeleteEntity = exceptionDeleteEntity;
	}

	public int getExceptionCreateAccount() {
		return exceptionCreateAccount;
	}

	public void setExceptionCreateAccount(int exceptionCreateAccount) {
		this.exceptionCreateAccount = exceptionCreateAccount;
	}

	public int getSuccessCreateAccount() {
		return successCreateAccount;
	}

	public void setSuccessCreateAccount(int successCreateAccount) {
		this.successCreateAccount = successCreateAccount;
	}

	public int getExceptionUpdateAccount() {
		return exceptionUpdateAccount;
	}

	public void setExceptionUpdateAccount(int exceptionUpdateAccount) {
		this.exceptionUpdateAccount = exceptionUpdateAccount;
	}

	public int getSuccessUpdateAccount() {
		return successUpdateAccount;
	}

	public void setSuccessUpdateAccount(int successUpdateAccount) {
		this.successUpdateAccount = successUpdateAccount;
	}

	public int getExceptionDeleteAccount() {
		return exceptionDeleteAccount;
	}

	public void setExceptionDeleteAccount(int exceptionDeleteAccount) {
		this.exceptionDeleteAccount = exceptionDeleteAccount;
	}

	public int getSuccessDeleteAccount() {
		return successDeleteAccount;
	}

	public void setSuccessDeleteAccount(int successDeleteAccount) {
		this.successDeleteAccount = successDeleteAccount;
	}

}
