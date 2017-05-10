package eu.bcvsolutions.idm.core.scheduler.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

public class IdmScheduledTaskFilter extends QuickFilter {

	private String quartzTaskName;

	public String getQuartzTaskName() {
		return quartzTaskName;
	}

	public void setQuartzTaskName(String quartzTaskName) {
		this.quartzTaskName = quartzTaskName;
	}

}
