package eu.bcvsolutions.idm.rpt.api.renderer;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 *  Template for report renderer
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractReportRenderer implements ReportRenderer {

	@Qualifier("objectMapper")
	@Autowired private ObjectMapper mapper;
	@Autowired private ConfigurationService configurationService;
	@Autowired private AttachmentManager attachmentManager;
	
	@Override
	public boolean supports(String delimiter) {
		return getName().equals(delimiter);
	}
	
	/**
	 * Returns reports json data as input stream
	 * 
	 * @param report
	 * @return
	 */
	protected InputStream getReportData(RptReportDto report) {
		Assert.notNull(report);
		Assert.notNull(report.getData());
		//
		return attachmentManager.getAttachmentData(report.getData());
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
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
