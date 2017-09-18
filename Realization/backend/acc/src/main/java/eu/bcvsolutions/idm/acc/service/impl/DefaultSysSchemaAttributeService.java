package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.event.SchemaAttributeEvent;
import eu.bcvsolutions.idm.acc.event.SchemaAttributeEvent.SchemaAttributeEventType;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default schema attributes
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeService extends AbstractReadWriteDtoService<SysSchemaAttributeDto, SysSchemaAttribute, SchemaAttributeFilter>
		implements SysSchemaAttributeService {

	private final SysSchemaAttributeRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}
	
	@Override
	protected Page<SysSchemaAttribute> findEntities(SchemaAttributeFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttributeDto schemaAttribute, BasePermission... permission) {
		Assert.notNull(schemaAttribute);
		//
		checkAccess(this.getEntity(schemaAttribute.getId()), permission);
		//
		entityEventManager.process(new SchemaAttributeEvent(SchemaAttributeEventType.DELETE, schemaAttribute));
	}
	
	@Override
	public SysSchemaAttributeDto clone(UUID id) {
		SysSchemaAttributeDto original = this.get(id);
		Assert.notNull(original, "Schema attribute must be found!");

		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}
}
