package eu.bcvsolutions.idm.rpt.report.general;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.FormableDto;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implemetation of general entity report. This actiot will be available for all
 * formable entities.
 *
 * @author Vít Švanda
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component
@Enabled(RptModuleDescriptor.MODULE_ID)
public class GeneralEntityExport extends AbstractFormableEntityExport<FormableDto, BaseFilter> {

	@Autowired
	LookupService lookupService;
	private ReadWriteDtoService<FormableDto, BaseFilter> localService;

	public GeneralEntityExport(RptReportService reportService, AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {

		super(reportService, attachmentManager, mapper, formService);
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		ReadWriteDtoService<FormableDto, BaseFilter> service = getService();

		if (service instanceof AuthorizableService) {
			AuthorizableService authorizableService = (AuthorizableService) service;
			AuthorizableType authorizableType = authorizableService.getAuthorizableType();
			boolean readPermissionFound = authorizableType.getGroup().getPermissions()
					.stream()
					.filter(permission -> IdmBasePermission.READ == permission)
					.findFirst()
					.isPresent();
			if (readPermissionFound) {
				// If exist, read permission for that type will be returned.
				return Lists.newArrayList(
						MessageFormat.format("{0}{1}{2}",
								authorizableType.getGroup().getName(),
								IdmBasePermission.SEPARATOR,
								IdmBasePermission.READ.name())
				);
			}
		} else {
			// Service is not authorizable -> none authorities are required.
			return Lists.newArrayList();
		}
		// By default only super admin can use report.
		return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
	}

	@Override
	/**
	 * Get service dynamicaly by action.
	 */
	public ReadWriteDtoService<FormableDto, BaseFilter> getService() {
		if (localService != null) {
			return localService;
		}

		try {
			IdmBulkActionDto action = getAction();
			if (action == null) {
				return null;
			}

			localService = (ReadWriteDtoService<FormableDto, BaseFilter>) lookupService
					.getDtoService((Class<? extends Identifiable>) Class.forName(action.getEntityClass()));
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(GeneralEntityExport.class.getName()).log(Level.SEVERE, null, ex);
		}
		return localService;
	}

	@Override
	public boolean supports(Class<? extends BaseEntity> clazz) {
		return FormableEntity.class.isAssignableFrom(clazz);
	}
}
