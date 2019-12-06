package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIncompatibleRole_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityIncompatibleRoleDto;

/**
 * List of identities with assigned incompatible roles
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(value = IdentityIncompatibleRoleReportExecutor.REPORT_NAME)
@Enabled(RptModuleDescriptor.MODULE_ID)
@Description("Identitities - assigned incompatible roles")
public class IdentityIncompatibleRoleReportExecutor extends AbstractReportExecutor {
	
	public static final String REPORT_NAME = "identity-incompatible-role-report"; // report ~ executor name
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIncompatibleRoleService incompatibleRoleService;
 
	/**
	 * Report ~ executor name
	 */
	@Override
	public String getName() {
		return REPORT_NAME;
	}
 
	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		// prepare temp file for json stream
		File temp = getAttachmentManager().createTempFile();
		//
		try (FileOutputStream outputStream = new FileOutputStream(temp);) {
	        // write into json stream
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				// json will be array of identities
				jGenerator.writeStartArray();		
				// form instance has useful methods to transform form values
				Pageable pageable = PageRequest.of(0, 100, new Sort(Direction.ASC, IdmIdentity_.username.getName()));

				//
				counter = 0L;
				do {
					Page<IdmIdentityDto> identities = identityService.find(null, pageable, IdmBasePermission.READ);
					if (count == null) {
						count = identities.getTotalElements();
					}
					boolean canContinue = true;
					for (Iterator<IdmIdentityDto> i = identities.iterator(); i.hasNext() && canContinue;) {						
						IdmIdentityDto identity = i.next();
						// search assigned roles
						IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
						filter.setIdentityId(identity.getId());
						filter.setDirectRole(Boolean.TRUE); // direct roles only
						List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null, IdmBasePermission.READ).getContent();
						// search incompatible roles
						Set<ResolvedIncompatibleRoleDto> incompatibleRoles = incompatibleRoleService.resolveIncompatibleRoles(
								identityRoles
									.stream()
									.map(ir -> ir.getRole())
									.collect(Collectors.toList())
								);
						for (ResolvedIncompatibleRoleDto resolvedIncompatibleRole : incompatibleRoles) {
							// add item into report
							RptIdentityIncompatibleRoleDto reportItem = new RptIdentityIncompatibleRoleDto();
							reportItem.setIdentity(identity);
							reportItem.setDirectRole(resolvedIncompatibleRole.getDirectRole());
							reportItem.setIncompatibleRole(resolvedIncompatibleRole.getIncompatibleRole());
							// dtos in embedded cannot be parsed from json automatically as objects => aaet them into report dto directly
							IdmRoleDto superior = DtoUtils.getEmbedded(resolvedIncompatibleRole.getIncompatibleRole(), IdmIncompatibleRole_.superior);
							IdmRoleDto sub = DtoUtils.getEmbedded(resolvedIncompatibleRole.getIncompatibleRole(), IdmIncompatibleRole_.sub);
							reportItem.setSuperior(superior);
							reportItem.setSub(sub);
							//
							getMapper().writeValue(jGenerator, reportItem);	
						}
						// supports cancel report generating (report extends long running task)
						++counter;
						canContinue = updateState();
					}		
					// iterate while next page of identities is available
					pageable = identities.hasNext() && canContinue ? identities.nextPageable() : null;
				} while (pageable != null);
				//
				// close array of identities
				jGenerator.writeEndArray();
			} finally {
				// close json stream
				jGenerator.close();
			}
			// save create temp file with array of identities in json as attachment
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			FileUtils.deleteQuietly(temp);
		}
	}
}