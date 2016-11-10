package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/audits")
public class IdmAuditController extends AbstractReadEntityController<IdmAudit, AuditFilter> {
	
	@Autowired
	private IdmAuditService auditService;
	
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
	
	@RequestMapping(value= "/search/entities", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<String>> findAuditedEntity(@PageableDefault Pageable pageable, PersistentEntityResourceAssembler assembler) {
		// TODO: pageable is necessary? 
		// TODO: helpful more info about entities, links? repository?
		List<String> entities = auditService.getAllAuditedEntitiesNames();
		ResourcesWrapper<String> resource = new ResourcesWrapper<>(entities);
		return new ResponseEntity<ResourcesWrapper<String>>(resource, HttpStatus.OK);
	}
	
	public AuditFilter toFilter(MultiValueMap<String, Object> parameters) {
		AuditFilter filter = new AuditFilter();
		filter.setModification(convertStringParameter(parameters, "modification"));
		filter.setModifier(convertStringParameter(parameters, "modifier"));
		filter.setText(convertStringParameter(parameters, "attributes"));
		filter.setFrom(convertDateTimeParameter(parameters, "from"));
		filter.setTo(convertDateTimeParameter(parameters, "to"));
		filter.setEntityId(convertUuidParameter(parameters, "entityId"));
		filter.setType(convertStringParameter(parameters, "type"));
		return filter;
	}
}
