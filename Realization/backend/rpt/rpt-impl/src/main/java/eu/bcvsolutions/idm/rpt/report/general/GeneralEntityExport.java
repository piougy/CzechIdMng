package eu.bcvsolutions.idm.rpt.report.general;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Implemetation of general entity report. This actiot will be available for all
 * formable entities.
 *
 * @author Vít Švanda
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
@Component
@Enabled(RptModuleDescriptor.MODULE_ID)
public class GeneralEntityExport extends AbstractFormableEntityExport<FormableDto, BaseFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GeneralEntityExport.class);
	
	@Autowired
	private LookupService lookupService;
	private ReadWriteDtoService<FormableDto, BaseFilter> localService;

	public GeneralEntityExport(RptReportService reportService, AttachmentManager attachmentManager, ObjectMapper mapper, FormService formService) {

		super(reportService, attachmentManager, mapper, formService);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected List<String> getAuthoritiesForEntity() {
		ReadWriteDtoService<FormableDto, BaseFilter> service = getService();

		if (!(service instanceof AuthorizableService)) {
			// Service is not authorizable => only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
		
		AuthorizableService authorizableService = (AuthorizableService) service;
		AuthorizableType authorizableType = authorizableService.getAuthorizableType();
		if (authorizableType == null) {
			// Service is authorizable but group is not specified => only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
			
		boolean readPermissionFound = authorizableType.getGroup().getPermissions()
				.stream()
				.filter(permission -> IdmBasePermission.READ == permission)
				.findFirst()
				.isPresent();
		if (!readPermissionFound) {
			// By default only super admin can use report.
			return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
		}
		
		// If exist, read permission for that type will be returned.
		return Lists.newArrayList(
				MessageFormat.format("{0}{1}{2}",
						authorizableType.getGroup().getName(),
						IdmBasePermission.SEPARATOR,
						IdmBasePermission.READ.name())
		);
	}
	
	/**
	 * Get service dynamically by action.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ReadWriteDtoService<FormableDto, BaseFilter> getService() {
		if (localService != null) {
			return localService;
		}
		
		IdmBulkActionDto action = getAction();
		if (action == null) {
			return null;
		}
		
		try {
			localService = (ReadWriteDtoService<FormableDto, BaseFilter>) lookupService
					.getDtoService((Class<? extends Identifiable>) Class.forName(action.getEntityClass()));
		} catch (ClassNotFoundException ex) {
			LOG.error("Service for entity type [{}] is not registered in lookup.", action.getEntityClass(), ex);
		}
		return localService;
	}

	@Override
	public boolean supports(Class<? extends BaseEntity> clazz) {
		return FormableEntity.class.isAssignableFrom(clazz);
	}
}
