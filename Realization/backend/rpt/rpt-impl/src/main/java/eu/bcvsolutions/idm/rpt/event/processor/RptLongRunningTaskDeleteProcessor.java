package eu.bcvsolutions.idm.rpt.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Removes link to LRT from generated reports, when LRT is deleted.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@Component(RptLongRunningTaskDeleteProcessor.PROCESSOR_NAME)
@Description("Removes link to LRT from generated reports, when LRT is deleted.")
public class RptLongRunningTaskDeleteProcessor
		extends CoreEventProcessor<IdmLongRunningTaskDto> {
	
	public static final String PROCESSOR_NAME = "rpt-long-running-task-delete-processor";
	//
	@Autowired private RptReportService service;
	
	public RptLongRunningTaskDeleteProcessor() {
		super(LongRunningTaskEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		IdmLongRunningTaskDto lrt = event.getContent();
		Assert.notNull(lrt.getId(), "Long running task identifier is required.");
		//		
		RptReportFilter filter = new RptReportFilter();
		filter.setLongRunningTaskId(lrt.getId());
		service
			.find(filter, null)
			.forEach(report -> {
				report.setLongRunningTask(null);
				//
				service.save(report);
			});
		//
		return new DefaultEventResult<>(event, this);
	}
}