package eu.bcvsolutions.idm.core.scheduler.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;

public class IdmScheduledTaskFilter extends DataFilter {

	private String quartzTaskName;
	
	public IdmScheduledTaskFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmScheduledTaskFilter(MultiValueMap<String, Object> data) {
		super(IdmScheduledTaskDto.class, data);
	}

	public String getQuartzTaskName() {
		return quartzTaskName;
	}

	public void setQuartzTaskName(String quartzTaskName) {
		this.quartzTaskName = quartzTaskName;
	}

}
