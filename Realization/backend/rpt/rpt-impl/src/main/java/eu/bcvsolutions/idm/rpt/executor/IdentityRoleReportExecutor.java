package eu.bcvsolutions.idm.rpt.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleDto;

/**
 * Identity - role reports.
 * - identities and their roles
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Identities and their roles")
public class IdentityRoleReportExecutor extends AbstractReportExecutor {

	public static final String REPORT_NAME = "identity-role-report";
	private static final String PARAMETER_VALID_ROLES = "validRoles";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Override
	public String getName() {
		return REPORT_NAME;
	}
	
	@Override
	public String getModule() {
		return RptModuleDescriptor.MODULE_ID;
	}
	
	@Override
	protected List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.add(new IdmFormAttributeDto(PARAMETER_VALID_ROLES, "Valid roles only", PersistentType.BOOLEAN));
		return attributes;
	}
	
	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		File temp = null;
		FileOutputStream outputStream = null;
		try {
			temp = getAttachmentManager().createTempFile();
	        outputStream = new FileOutputStream(temp);
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				jGenerator.writeStartArray();				
				IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
				MultiValueMap<String, Object> filterParameters = formInstance.toMultiValueMap();
				//
				counter = 0L;
				Pageable pageable = new PageRequest(0, 100, new Sort(Direction.ASC, IdmIdentity_.username.getName()));
				do {
					Page<IdmIdentityDto> identities = identityService.find(pageable, IdmBasePermission.READ);
					if (count == null) {
						count = identities.getTotalElements();
					}
					boolean canContinue = true;
					for (Iterator<IdmIdentityDto> i = identities.iterator(); i.hasNext() && canContinue;) {
						writeIdentityRoles(jGenerator, i.next(), (Boolean) filterParameters.getFirst(PARAMETER_VALID_ROLES));
						//
						++counter;
						canContinue = updateState();
					}			
					pageable = identities.hasNext() && canContinue ? identities.nextPageable() : null;
				} while (pageable != null);
				//
				jGenerator.writeEndArray();
			} finally {
				jGenerator.close();
			}
			//
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			IOUtils.closeQuietly(outputStream); // just for sure - jGenerator should close stream itself
			FileUtils.deleteQuietly(temp);
		}
	}
	
	private void writeIdentityRoles(JsonGenerator jGenerator, IdmIdentityDto identity, Boolean validRoles) 
			throws JsonGenerationException, JsonMappingException, IOException {
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setValid(validRoles);
		
		boolean hasRole = false;
		Pageable pageable = new PageRequest(0, 100, new Sort(IdmIdentityRole_.role.getName() + "." + IdmRole_.name.getName()));
		do {
			Page<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, pageable, IdmBasePermission.READ);
			for (IdmIdentityRoleDto identityRole: identityRoles) {
				getMapper().writeValue(jGenerator, new RptIdentityRoleDto(identity, identityRole));
				hasRole = true;
			}
			pageable = identityRoles.hasNext() ? identityRoles.nextPageable() : null;
		} while (pageable != null);
		//
		if (!hasRole) {
			// identity doesn't have any roles - add identity only
			getMapper().writeValue(jGenerator, new RptIdentityRoleDto(identity));
		}
	}
}
