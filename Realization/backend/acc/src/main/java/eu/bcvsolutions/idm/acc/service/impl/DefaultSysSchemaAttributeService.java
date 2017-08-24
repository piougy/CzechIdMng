package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
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

	private final SysSystemAttributeMappingService systeAttributeMappingService;

	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			SysSystemAttributeMappingService systeAttributeMappingService) {
		super(repository);
		//
		Assert.notNull(systeAttributeMappingService);
		//
		this.systeAttributeMappingService = systeAttributeMappingService;
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttributeDto schemaAttribute, BasePermission... permission) {
		Assert.notNull(schemaAttribute);
		// 
		// remove all handled attributes
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());
		systeAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systeAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		super.delete(schemaAttribute, permission);
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
