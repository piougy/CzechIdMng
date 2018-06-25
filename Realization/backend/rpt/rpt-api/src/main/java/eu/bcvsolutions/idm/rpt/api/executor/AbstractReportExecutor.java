package eu.bcvsolutions.idm.rpt.api.executor;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;

/**
 * Template for report executor
 * 
 * TODO: generalize schedulable LRT
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractReportExecutor 
		extends AbstractLongRunningTaskExecutor<RptReportDto>
		implements ReportExecutor {

	@Qualifier("objectMapper")
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
	
	@Override
	public RptReportDto generate(RptReportDto report) {
		try {
			IdmAttachmentDto data = generateData(report);
			report.setData(data.getId());
			return report;
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
		// event is empty - publish new event and stop current LRT process (event has to be initialized at first)
		if (getLongRunningTaskId() != null) {
			IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
			if (task != null) {
				task.setRunning(false);
				getLongRunningTaskService().save(task);
			}
		}
		// executed from scheduler => we propagate new event for creating report
		RptReportDto report = new RptReportDto();
		report.setExecutorName(getName());
		report.setLongRunningTask(getLongRunningTaskId());
		reportManager.generate(report);
		return report;		
	}
	
	@Override
	protected RptReportDto end(RptReportDto result, Exception ex) {
		result = super.end(result, ex);
		//
		// continue generate event, if event is given
		if (event != null) {
			entityEventManager.process(event);
		}
		// 
		return result;	
	}
	
	/**
	 * Saves (temporrary) file as attachment
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
		if (formDefinition.getFormAttributes().isEmpty()) {
			return null;
		}
		// check form definition for given executor
		// incompatible changes has to be solved by change script or by adding new executor
		// adding parameter is compatible change
		return formDefinitionService.updateDefinition(RptReportDto.class, getName(), formDefinition.getFormAttributes());
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
