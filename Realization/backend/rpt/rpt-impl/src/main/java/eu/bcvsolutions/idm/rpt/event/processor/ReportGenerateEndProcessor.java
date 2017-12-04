package eu.bcvsolutions.idm.rpt.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent.ReportEventType;
import eu.bcvsolutions.idm.rpt.api.event.processor.ReportProcessor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Save generated report metadata by long running task
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Save generated report metadata by long running task.")
public class ReportGenerateEndProcessor 
		extends CoreEventProcessor<RptReportDto> 
		implements ReportProcessor {
	
	public static final String PROCESSOR_NAME = "report-generate-end-processor";
	//
	@Autowired private RptReportService reportService;
	
	public ReportGenerateEndProcessor() {
		super(ReportEventType.GENERATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<RptReportDto> process(EntityEvent<RptReportDto> event) {
		RptReportDto report = event.getContent();
		report = reportService.saveInternal(report);
		event.setContent(report);
		//
		return new DefaultEventResult<>(event, this);	
	}
	
	@Override
	public int getOrder() {
		return 0;
	}

}
