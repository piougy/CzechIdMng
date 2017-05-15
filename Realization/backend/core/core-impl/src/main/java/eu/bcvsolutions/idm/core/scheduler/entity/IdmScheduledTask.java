package eu.bcvsolutions.idm.core.scheduler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Scheduled tasks are a control mechanism for stateful long running
 * tasks run by quartz. The purpose is to aggregate {@link IdmLongRunningTask}
 * as with processed items log and an item queue of already
 * processed records.
 * 
 * @author Jan Helbich
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "idm_scheduled_task")
public class IdmScheduledTask extends AbstractEntity {

	@Audited
	@NotNull
	@Column(name = "quartz_task_name", unique = true)
	private String quartzTaskName;

	@Audited
	@Column(name = "dry_run")
	private boolean dryRun;

	public String getQuartzTaskName() {
		return quartzTaskName;
	}

	public void setQuartzTaskName(String quartzTaskName) {
		this.quartzTaskName = quartzTaskName;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

}
