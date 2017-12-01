package eu.bcvsolutions.idm.rpt.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
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
 * Role - identity reports.
 * - roles and their identities
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Roles and their identities")
public class RoleIdentityReportExecutor extends AbstractReportExecutor {

	public static final String REPORT_NAME = "role-identity-report";
	private static final String PARAMETER_VALID_ROLES = "validRoles";
	private static final String PARAMETER_ROLES = "roles";
	//	
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityService identityService;
	
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
		IdmFormAttributeDto roles = new IdmFormAttributeDto(PARAMETER_ROLES, "Roles", PersistentType.UUID);
		roles.setMultiple(true);
		roles.setFaceType(BaseFaceType.ROLE_SELECT);
		roles.setPlaceholder("All roles or select ...");
		attributes.add(roles);
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
				List<Object> roleIds = filterParameters.get(PARAMETER_ROLES);
				//
				counter = 0L;
				if (roleIds != null && !roleIds.isEmpty()) {
					count = Long.valueOf(roleIds.size());
					for (Object roleId : roleIds) {
						IdmRoleDto role = roleService.get(EntityUtils.toUuid(roleId), IdmBasePermission.READ); // TODO: catch or skip forbidden exception?
						writeIdentityRoles(jGenerator, role, (Boolean) filterParameters.getFirst(PARAMETER_VALID_ROLES));
						//
						++counter;
						if(!updateState()) {
							break;
						}
					}
				} else {				
					Pageable pageable = new PageRequest(0, 100, new Sort(Direction.ASC, IdmRole_.name.getName()));
					do {
						Page<IdmRoleDto> roles = roleService.find(pageable, IdmBasePermission.READ);
						if (count == null) {
							count = roles.getTotalElements();
						}
						boolean canContinue = true;
						for (Iterator<IdmRoleDto> i = roles.iterator(); i.hasNext() && canContinue;) {
							writeIdentityRoles(jGenerator, i.next(), (Boolean) filterParameters.getFirst(PARAMETER_VALID_ROLES));
							//
							++counter;
							canContinue = updateState();
						}			
						pageable = roles.hasNext() && canContinue ? roles.nextPageable() : null;
					} while (pageable != null);
				}
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
	
	private void writeIdentityRoles(JsonGenerator jGenerator, IdmRoleDto role, Boolean validRoles)
			throws JsonGenerationException, JsonMappingException, IOException {
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleId(role.getId());
		filter.setValid(validRoles);
		
		boolean hasIdentity = false;
		Pageable pageable = new PageRequest(0, 100, new Sort(
				Direction.ASC,
				IdmIdentityRole_.identityContract.getName() + "." + IdmIdentityContract_.identity.getName() + "." + IdmIdentity_.username.getName()));
		do {
			Page<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, pageable, IdmBasePermission.READ);
			for (IdmIdentityRoleDto identityRole: identityRoles) {
				IdmIdentityContractDto contract = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract.getName(), IdmIdentityContractDto.class);
				getMapper().writeValue(jGenerator, new RptIdentityRoleDto(identityService.get(contract.getIdentity()), identityRole));
				hasIdentity = true;
			}
			pageable = identityRoles.hasNext() ? identityRoles.nextPageable() : null;
		} while (pageable != null);
		//
		if (!hasIdentity) {
			// role doesn't have any identities - add identity only
			getMapper().writeValue(jGenerator, new RptIdentityRoleDto(role));
		}
	}
	
}
