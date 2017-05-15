package eu.bcvsolutions.idm.core.scheduler.api.dto;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Idm scheduled task DTO.
 *
 * @author Jan Helbich
 *
 */
@SuppressWarnings("serial")
public class IdmScheduledTaskDto extends AbstractDto {

	private String quartzTaskName;
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
