package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;

/**
 * EAV Form definitions
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-attributes")
public class IdmFormAttributeController extends DefaultReadWriteEntityController<IdmFormAttribute, FormAttributeFilter>  {

	@Autowired
	public IdmFormAttributeController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	public void deleteEntity(IdmFormAttribute entity) {
		// attribute flagged as system attribute can't be deleted from controller
		if (entity.isSystemAttribute()) {
			throw new ResultCodeException(CoreResultCode.FORM_ATTRIBUTE_DELETE_FAILED_SYSTEM_ATTRIBUTE, ImmutableMap.of("name", entity.getName()));
		}
		super.deleteEntity(entity);
	}
}
