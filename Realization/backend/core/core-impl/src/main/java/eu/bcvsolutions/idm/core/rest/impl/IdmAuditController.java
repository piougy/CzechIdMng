package eu.bcvsolutions.idm.core.rest.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.PropertyUtils;
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

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
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
		// TODO get revision detail
		AuditFilter filter = new AuditFilter();
		filter.setId(Long.parseLong(backendId));
		List<IdmAudit> audits = auditService.find(filter, null).getContent();
		
		// number founds audits must be exactly 1
		if (audits.isEmpty() || audits.size() != 1) {
			// throw..
		}
		
		IdmAudit audit = audits.get(0);
		
		// Map with all values
		Map<String, Object> revisionValues = new HashMap<>();
		
		Object revision = null;
		try {
			revision = auditService.getPreviousVersion(Class.forName(audit.getType()), audit.getEntityId(), Long.parseLong(audit.getId().toString()));
		} catch (NumberFormatException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> auditedClass = auditService.getAllAuditedEntitiesNames();
		
		Field[] fields = revision.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(revision, field.getName());
				
				// not all property must have read method
				if (propertyDescriptor == null) {
					continue;
				}
				
				Method readMethod = propertyDescriptor.getReadMethod();
				Object value = readMethod.invoke(revision);
				
				// value can be null
				if (value == null) {
					continue;
				}
				
				// we want only primitive date types
				String className = value.getClass().getSimpleName();
				if (className.indexOf("_", 0) > 0 && auditedClass.contains(className.substring(0, className.indexOf("_", 0)))) {
					revisionValues.put(field.getName(), ((AbstractEntity)value).getId());
				} else {
					revisionValues.put(field.getName(), value);
				}
				
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// DTO
		IdmAuditDto auditDto = new IdmAuditDto(audit);
		auditDto.setRevisionValues(revisionValues);
		
		ResponseEntity<IdmAuditDto> resource = new ResponseEntity<IdmAuditDto>(auditDto, HttpStatus.OK);
		
		return resource;
	}
	
	public AuditFilter toFilter(MultiValueMap<String, Object> parameters) {
		AuditFilter filter = new AuditFilter();
		filter.setModification(getParameterConverter().toString(parameters, "modification"));
		filter.setModifier(getParameterConverter().toString(parameters, "modifier"));
		filter.setText(getParameterConverter().toString(parameters, "attributes"));
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		filter.setEntityId(getParameterConverter().toUuid(parameters, "entityId"));
		filter.setType(getParameterConverter().toString(parameters, "type"));
		return filter;
	}
}
