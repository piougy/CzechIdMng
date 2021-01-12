package eu.bcvsolutions.idm.rpt.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
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
public class TestFilterReportExecutor extends AbstractReportExecutor {

	protected static final String REPORT_NAME = "test-filter-report";
	
	@Override
	public String getName() {
		return REPORT_NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto attribute = new IdmFormAttributeDto(IdmIdentity_.username.getName(), "Username", PersistentType.TEXT);
		attribute.setMultiple(true);
		//
		return Lists.newArrayList(attribute);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected IdmAttachmentDto generateData(RptReportDto report) {
		try {
			IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
			//
			List usernames = (List) formInstance.toPersistentValues(IdmIdentity_.username.getName());
			List<IdmIdentityDto> results = TestReportExecutor.identities
					.stream()
					.filter(identity -> {						
						return ObjectUtils.isEmpty(usernames) || usernames.contains(identity.getUsername());
					})
					.collect(Collectors.toList());
			return createAttachment(report, IOUtils.toInputStream(getMapper().writeValueAsString(results)));
		} catch (FileNotFoundException | JsonProcessingException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		}
	}


}
