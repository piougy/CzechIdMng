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
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;

/**
 * Ends long running task and persists him.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Generate report by long running task.")
public class ReportGenerateProcessor 
		extends CoreEventProcessor<RptReportDto> 
		implements ReportProcessor {
	
	public static final String PROCESSOR_NAME = "report-generate-processor";
	//
	@Autowired private ReportManager reportManager;
	
	public ReportGenerateProcessor() {
		super(ReportEventType.GENERATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<RptReportDto> process(EntityEvent<RptReportDto> event) {
		// execute LRT
		try {
			// we want be sure event is suspended
			return new DefaultEventResult
					.Builder<>(event, this)
					.setSuspended(true)
					.build();
		} finally {
			// then generate report
			reportManager.generate(event);
		}
		
	}
	
	@Override
	public int getOrder() {
		return -1000;
	}

}
