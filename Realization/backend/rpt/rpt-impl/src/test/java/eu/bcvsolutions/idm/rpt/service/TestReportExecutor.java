package eu.bcvsolutions.idm.rpt.service;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;

/**
 * Test identity reports.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Test identieties report")
public class TestReportExecutor extends AbstractReportExecutor {

	protected static final String REPORT_NAME = "test-report";
	protected static final List<IdmIdentityDto> identities;
	
	static {
		IdmIdentityDto identityOne = new IdmIdentityDto("one");
		IdmIdentityDto identityTwo = new IdmIdentityDto("two");
		identities = Lists.newArrayList(identityOne, identityTwo);
	}
	
	@Override
	public String getName() {
		return REPORT_NAME;
	}
	
	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		try {
			return createAttachment(report, IOUtils.toInputStream(getMapper().writeValueAsString(identities)));
		} catch (FileNotFoundException | JsonProcessingException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		}
	}


}
