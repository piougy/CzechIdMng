package eu.bcvsolutions.idm.rpt.event.processor;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto.Builder;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.domain.RptResultCode;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent.ReportEventType;
import eu.bcvsolutions.idm.rpt.api.event.processor.ReportProcessor;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.renderer.DefaultJsonRenderer;

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
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReportGenerateEndSendNotificationProcessor.class);
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private ConfigurationService configurationService;
	@Autowired private ReportManager reportManager;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private FormService formService;
	
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
		UUID creatorId = report.getCreatorId();
		//
		if (report.getResult() != null) {
			boolean success = report.getResult().getState() == OperationState.EXECUTED;
			List<IdmIdentityDto> recipients = new ArrayList<>(1);
			if (creatorId != null) {
				// default recipient is logged user, but can be overriden by topic configuration
				recipients.add(identityService.get(creatorId));
			}
			//
			Builder message = new IdmMessageDto
					.Builder(success ? NotificationLevel.SUCCESS : NotificationLevel.WARNING)
					.addParameter("url", configurationService.getFrontendUrl(String.format("rpt/reports?id=%s", report.getId())))
					.addParameter("report", report)
					.setModel(new DefaultResultModel(
							success ? RptResultCode.REPORT_GENERATE_SUCCESS : RptResultCode.REPORT_GENERATE_FAILED, 
							ImmutableMap.of("reportName", report.getName())
					));
			//
			if (success) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				// render reports as email attachment
				List<IdmAttachmentDto> attachments = reportManager
						.getRenderers(report.getExecutorName())
						.stream()
						.filter(renderer -> !renderer.getName().equals(DefaultJsonRenderer.RENDERER_NAME)) // default json will be ignored
						.map(renderer -> {
							RptRenderedReportDto result = reportManager.render(report, renderer.getName());
							//
							// save rendered report as attachment
							IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
							attachmentDto.setDescription(getDescription());
							String reportName = String.format(
									"%s-%s.%s", 
									SpinalCase.format(report.getExecutorName()),
									report.getCreated().format(formatter),
									renderer.getExtension()
							);
							attachmentDto.setName(reportName);
							attachmentDto.setMimetype(renderer.getFormat().toString());
							attachmentDto.setInputData(result.getRenderedReport());
							//
							return attachmentManager.saveAttachment(report, attachmentDto);
						})
						.collect(Collectors.toList());
				//
				// load topic configuration
				String topic = null;
				IdmFormDto filter = report.getFilter();
				if (filter != null) {
					IdmFormInstanceDto formInstance = new IdmFormInstanceDto(
							report, 
							formService.getDefinition(filter.getFormDefinition()), 
							report.getFilter()
					);
					Serializable configuredTopic = formInstance.toSinglePersistentValue(
							AbstractReportExecutor.PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS
					);
					if (configuredTopic != null) {
						topic = configuredTopic.toString();
					}
				} else {
					// Backward compatibility => reports generated from code (without UI form + filter).
					topic = RptModuleDescriptor.TOPIC_REPORT_GENERATE_SUCCESS;
				}
				//
				// topic is optional => notification will not be sent, if default value is cleared / not given.
				if (StringUtils.isEmpty(topic)) {
					LOG.debug("Report result will be not sent, topic is not configured [{}].");
				} else {
					LOG.debug("Report result will be sent to topic [{}]", topic);
					//
					notificationManager.send(
							topic,
							message.build(),
							null,
							recipients,
							attachments);
				}
			} else if (creatorId != null) {
				notificationManager.send(
						RptModuleDescriptor.TOPIC_REPORT_GENERATE_FAILED,
						message.build(), 
						identityService.get(creatorId));
			}			
		}
		//
		return new DefaultEventResult<>(event, this);	
	}
	
	@Override
	public int getOrder() {
		return 1000;
	}

}
