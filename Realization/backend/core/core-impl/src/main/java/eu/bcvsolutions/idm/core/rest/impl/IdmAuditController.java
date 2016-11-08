package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.AuditClassMapping;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/audit")
public class IdmAuditController extends AbstractReadEntityController<IdmAudit, AuditFilter> {
	
	@Autowired
	public IdmAuditController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	public AuditFilter toFilter(MultiValueMap<String, Object> parameters) {
		AuditFilter filter = new AuditFilter();
		filter.setModification(convertStringParameter(parameters, "modification"));
		filter.setText(convertStringParameter(parameters, "attributes"));
		filter.setFrom(convertDateTimeParameter(parameters, "from"));
		filter.setTo(convertDateTimeParameter(parameters, "to"));
		filter.setEntity(convertEnumParameter(parameters, "entity", AuditClassMapping.class));
		return filter;
	}
}
