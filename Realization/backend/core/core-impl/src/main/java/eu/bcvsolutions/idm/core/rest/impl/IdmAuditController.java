package eu.bcvsolutions.idm.core.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmAuditDiffDto;
import eu.bcvsolutions.idm.core.model.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.model.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 * TODO: Use only DTO
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/audits")
public class IdmAuditController extends AbstractReadEntityController<IdmAudit, AuditFilter> {

	@Autowired
	private IdmAuditService auditService;
	
	@Autowired
	public IdmAuditController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return this.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/entities", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<String>> findAuditedEntity(@PageableDefault Pageable pageable, PersistentEntityResourceAssembler assembler) {
		// TODO: pageable is necessary? 
		// TODO: helpful more info about entities, links? repository?
		List<String> entities = auditService.getAllAuditedEntitiesNames();
		ResourcesWrapper<String> resource = new ResourcesWrapper<>(entities);
		return new ResponseEntity<ResourcesWrapper<String>>(resource, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	@Override
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmAudit audit = auditService.get(backendId);
		
		// Map with all values
		Map<String, Object> revisionValues = new HashMap<>();
		
		Object revision = null;
		try {
			revision = auditService.getVersion(Class.forName(audit.getType()), audit.getEntityId(), Long.valueOf(audit.getId().toString()));
		} catch (NumberFormatException | ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("audit", audit), e);
		}
		
		revisionValues = auditService.getValuesFromVersion(revision);
		
		// create DTO and fill with values from IdmAudit
		IdmAuditDto auditDto = new IdmAuditDto(audit);
		auditDto.setRevisionValues(revisionValues);
		
		ResponseEntity<IdmAuditDto> resource = new ResponseEntity<IdmAuditDto>(auditDto, HttpStatus.OK);
		
		return resource;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{revId}/diff/previous")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public ResponseEntity<?> previousVersion(@PathVariable @NotNull String revId, PersistentEntityResourceAssembler assembler) {
		IdmAudit currentAudit = auditService.get(revId);
		IdmAudit previousAudit;
		ResponseEntity<IdmAuditDto> resource = null;
		
		try {
			IdmAuditDto dto = null;
			previousAudit = auditService.getPreviousRevision(Long.valueOf(currentAudit.getId().toString()));
			
			// previous version dost'n exist
			if (previousAudit != null) {
				dto = new IdmAuditDto(previousAudit);
				dto.setRevisionValues(
						auditService.getValuesFromVersion(
								auditService.getPreviousVersion(
										Class.forName(previousAudit.getType()),
										previousAudit.getEntityId(),
										Long.valueOf(previousAudit.getId().toString()))));
				resource = new ResponseEntity<IdmAuditDto>(dto, HttpStatus.OK);
			} else {
				resource = new ResponseEntity<IdmAuditDto>(HttpStatus.NOT_FOUND);
			}
			
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", currentAudit.getType()), e);
		}
		
		return resource;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{firstRevId}/diff/{secondRevId}")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.AUDIT_READ + "')")
	public ResponseEntity<?> diff(@PathVariable @NotNull String firstRevId, @PathVariable String secondRevId, PersistentEntityResourceAssembler assembler) {
		IdmAuditDiffDto dto = new IdmAuditDiffDto();
		dto.setDiffValues(auditService.getDiffBetweenVersion(Long.parseLong(firstRevId), Long.valueOf(secondRevId)));
		dto.setIdFirstRevision(Long.valueOf(firstRevId));
		dto.setIdSecondRevision(Long.valueOf(secondRevId));
		
		ResponseEntity<IdmAuditDiffDto> resource = new ResponseEntity<IdmAuditDiffDto>(dto, HttpStatus.OK);
		return resource;
	}
}
