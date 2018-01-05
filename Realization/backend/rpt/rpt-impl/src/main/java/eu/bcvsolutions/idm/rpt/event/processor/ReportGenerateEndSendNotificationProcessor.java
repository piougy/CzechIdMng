package eu.bcvsolutions.idm.rpt.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent.ReportEventType;
import eu.bcvsolutions.idm.rpt.api.event.processor.ReportProcessor;

/**
 * Sends notification after report is generated to report creator.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Sends notification after report is generated to report creator.")
public class ReportGenerateEndSendNotificationProcessor 
		extends CoreEventProcessor<RptReportDto> 
		implements ReportProcessor {
	
	public static final String PROCESSOR_NAME = "report-generate-end-send-notification-processor";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private ConfigurationService configurationService;
	
	public ReportGenerateEndSendNotificationProcessor() {
		super(ReportEventType.GENERATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<RptReportDto> process(EntityEvent<RptReportDto> event) {
		RptReportDto report = event.getContent();
		//
		// TODO: can be improved by some configuration (specially for LRT)
		if (report.getCreatorId() != null && report.getResult() != null) {
			boolean success = report.getResult().getState() == OperationState.EXECUTED;
			notificationManager.send(
					success ? RptModuleDescriptor.TOPIC_REPORT_GENERATE_SUCCESS : RptModuleDescriptor.TOPIC_REPORT_GENERATE_FAILED,
					new IdmMessageDto
						.Builder(success ? NotificationLevel.SUCCESS : NotificationLevel.WARNING)
						.addParameter("url", configurationService.getFrontendUrl(String.format("report/reports?id=%s", report.getId())))
						.addParameter("report", report)
						.setModel(new DefaultResultModel(
								success ? RptResultCode.REPORT_GENERATE_SUCCESS : RptResultCode.REPORT_GENERATE_FAILED, 
								ImmutableMap.of("reportName", report.getName())
								))
						.build(), 
					identityService.get(report.getCreatorId()));
			
		}
		//
		return new DefaultEventResult<>(event, this);	
	}
	
	@Override
	public int getOrder() {
		return 1000;
	}

}
