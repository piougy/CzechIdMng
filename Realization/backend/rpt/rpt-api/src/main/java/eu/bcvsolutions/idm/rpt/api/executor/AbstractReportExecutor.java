package eu.bcvsolutions.idm.rpt.api.executor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;

/**
 * Template for report executor.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractReportExecutor 
		extends AbstractSchedulableTaskExecutor<RptReportDto>
		implements ReportExecutor {
	
	/**
	 * Notification after report is successfully generated can be sent to different topic 
	 * than default {@link RptModuleDescriptor#TOPIC_REPORT_GENERATE_SUCCESS}.
	 * All report can configure this property.
	 * 
	 * @since 10.6.0
	 */
	public static final String PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS = "core:topic-report-generate-success";

	@Autowired private ObjectMapper mapper;
	@Autowired private ConfigurationService configurationService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private ReportManager reportManager;
	@Autowired private IdmFormDefinitionService formDefinitionService;
	@Autowired private AttachmentManager attachmentManager;
	//
	private EntityEvent<RptReportDto> event;
	
	/**
	 * Returns generated report data.
	 * {@link IdmAttachmentDto} is used - report json data should be streamed into temp file
	 * 
	 * @param report
	 * @return
	 */
	protected abstract IdmAttachmentDto generateData(RptReportDto report);
	
	@Override
	public boolean supports(String delimiter) {
		return getName().equals(delimiter);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * LRT properties are propagated to report parameters (~form).
	 */
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 10.6.0 recoverable now
	 */
	@Override
	public boolean isRecoverable() {
		return true;
	}
	
	@Override
	public RptReportDto generate(RptReportDto report) {
		try {
			IdmAttachmentDto data = generateData(report);
			report.setData(data.getId());
			return report;
		} catch (ResultCodeException ex) {
			throw ex;
		} catch (Exception ex) {
			// TODO: better exception
			throw new CoreException(ex);
		}
	}
	
	@Override
	public RptReportDto process() {
		if (event != null) {
			// generate report
			return generate(event.getContent());
		}
		//
		// executed from scheduler => we propagate new event for creating report
		RptReportDto report = new RptReportDto();
		report.setExecutorName(getName());
		report.setLongRunningTask(getLongRunningTaskId());
		reportManager.generate(report);
		//
		return report;		
	}
	
	@Override
	protected RptReportDto end(RptReportDto result, Exception ex) {
		// continue generate event, if event is given
		if (ex == null && event != null) {
			entityEventManager.process(event);
			//
			return super.end(result, ex);
		}
		// report failed => log exception
		if (ex != null) {
			return super.end(result, ex);
		}
		// publish new event and stop current LRT process (event has to be initialized at first)
		// => prevent to end task to early
		return null;
	}
	
	/**
	 * Saves (temporary) file as attachment
	 * 
	 * @param report
	 * @param data
	 * @return
	 * @throws FileNotFoundException
	 */
	protected IdmAttachmentDto createAttachment(RptReportDto report, InputStream jsonData) throws FileNotFoundException {
		IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
		attachmentDto.setDescription(getDescription());
		attachmentDto.setName(getName());
		attachmentDto.setMimetype(MediaType.APPLICATION_JSON_UTF8.toString());
		attachmentDto.setInputData(jsonData);		
		return attachmentManager.saveAttachment(report, attachmentDto);
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	public void setEvent(EntityEvent<RptReportDto> event) {
		this.event = event;
	}
	
	@Override
	public IdmFormDefinitionDto getFormDefinition() {
		IdmFormDefinitionDto formDefinition = ReportExecutor.super.getFormDefinition();
		// add common report parameters
		IdmFormAttributeDto topicAttribute = getTopicAttribute();
		if (topicAttribute != null) {
			formDefinition.getFormAttributes().add(topicAttribute);
		}
		// no attributes
		if (formDefinition.getFormAttributes().isEmpty()) {
			return null;
		}
		// check form definition for given executor
		// incompatible changes has to be solved by change script or by adding new executor
		// adding parameter is compatible change
		formDefinition.setType(formDefinitionService.getOwnerType(RptReportDto.class));
		formDefinition.setCode(getName());
		formDefinition.setModule(getModule());
		//
		return formDefinitionService.updateDefinition(formDefinition);
	}
	
	/**
	 * Notification after report is successfully generated can be sent to different topic 
	 * than default {@link RptModuleDescriptor#TOPIC_REPORT_GENERATE_SUCCESS}.
	 * All report can configure this property.
	 * Return null, if notification is not needed.
	 * 
	 * @since 10.6.0
	 * @return
	 */
	protected IdmFormAttributeDto getTopicAttribute() {
		IdmFormAttributeDto topicAttribute = new IdmFormAttributeDto(
				PROPERTY_TOPIC_REPORT_GENERATE_SUCCESS, 
				"Topic", 
				PersistentType.SHORTTEXT // TODO: topic select
		);
		topicAttribute.setDefaultValue(RptModuleDescriptor.TOPIC_REPORT_GENERATE_SUCCESS);
		//
		return topicAttribute;
	}
	
	/**
	 * Returns common json object mapper
	 * 
	 * @return
	 */
	protected ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Returns attachment manager for saving report data
	 * 
	 * @return
	 */
	protected AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}
}
