package eu.bcvsolutions.idm.rpt.report.general;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Implemetation of general entity report for {@link IdmRoleDto}
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component
@Enabled(RptModuleDescriptor.MODULE_ID)
public class IdmRoleGeneralEntityExport extends AbstractFormableEntityExport<IdmRoleDto, IdmRoleFilter>{
	public IdmRoleGeneralEntityExport(ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> service,
									  RptReportService reportService, AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {
		super(service, reportService, attachmentManager, mapper, formService);
	}
}
