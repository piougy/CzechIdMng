package eu.bcvsolutions.idm.acc.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "synchronization_config_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSynchronizationConfig synchronizationConfig;

	@NotNull
	@Column(name = "running", nullable = false)
	private boolean running = false;

	@Column(name = "started")
	private LocalDateTime started;

	@Column(name = "ended")
	private LocalDateTime ended;

	@Lob
	@Column(name = "token")
	private String token;
	
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	@OneToMany(mappedBy = "syncLog", fetch = FetchType.LAZY)
	private List<SysSyncActionLog> syncActionLogs;


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

	public List<SysSyncActionLog> getSyncActionLogs() {
		if(this.syncActionLogs == null){
			this.syncActionLogs = new ArrayList<>();
		}
		return syncActionLogs;
	}

	public void setSyncActionLogs(List<SysSyncActionLog> syncActionLogs) {
		this.syncActionLogs = syncActionLogs;
	}
}
