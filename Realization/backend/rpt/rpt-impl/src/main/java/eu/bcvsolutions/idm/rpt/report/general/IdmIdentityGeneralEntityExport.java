package eu.bcvsolutions.idm.rpt.report.general;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Implemetation of general entity report for {@link IdmIdentityDto}
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component
@Enabled(RptModuleDescriptor.MODULE_ID)
public class IdmIdentityGeneralEntityExport extends AbstractFormableEntityExport<IdmIdentityDto, IdmIdentityFilter>{
	public IdmIdentityGeneralEntityExport(ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> service,
										  RptReportService reportService, AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {
		super(service, reportService, attachmentManager, mapper, formService);
	}
}
