package eu.bcvsolutions.idm.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteEntityController;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * EAV Form definitions
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/form-definitions")
public class IdmFormDefinitionController extends DefaultReadWriteEntityController<IdmFormDefinition, EmptyFilter>  {

	@Autowired
	public IdmFormDefinitionController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
}
