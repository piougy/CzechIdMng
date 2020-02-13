package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation for duplication of the system mapping
 * 
 * @author Ondrej Husnik
 *
 */

@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(SystemMappingDuplicateBulkAction.NAME)
@Description("Bulk operation to duplicate the system mapping.")
public class SystemMappingDuplicateBulkAction extends AbstractBulkAction<SysSystemMappingDto, SysSystemMappingFilter> {

	public static final String NAME = "acc-system-mapping-duplicate-bulk-action";

	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSchemaAttributeService attributeService;

	@Override
	protected OperationResult processDto(SysSystemMappingDto dto) {
		Assert.notNull(dto, "System mapping DTO must not be null!");
		Assert.notNull(dto.getId(), "Id of the system mapping is required!");
		// Check rights
		mappingService.checkAccess(mappingService.get(dto.getId(), IdmBasePermission.READ), IdmBasePermission.UPDATE);

		Map<UUID, UUID> schemaAttributesCache = new HashMap<UUID, UUID>();
		Map<UUID, UUID> mappedAttributesCache = new HashMap<UUID, UUID>();
		UUID schemaId = dto.getObjectClass();
		Assert.notNull(schemaId, "Schema Id cannot be null!");
		SysSchemaObjectClassDto schemaDto = schemaService.get(schemaId);
		Assert.notNull(schemaDto, "Schema cannot be null!");

		// Put all schema attributes for given schema without cloning just copy 1-to-1
		SysSchemaAttributeFilter schemaAttributesFilter = new SysSchemaAttributeFilter();
		schemaAttributesFilter.setObjectClassId(schemaId);
		attributeService.find(schemaAttributesFilter, null).getContent() //
				.stream() //
				.forEach(schemaAttribute -> {
					UUID attrId = schemaAttribute.getId();
					schemaAttributesCache.put(attrId, attrId);
				});

		// Duplicate the system mapping
		mappingService.duplicateMapping(dto.getId(), schemaDto, schemaAttributesCache, mappedAttributesCache, true);

		return new OperationResult(OperationState.EXECUTED);
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.SYSTEM_READ, AccGroupPermission.SYSTEM_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

	@Override
	public ReadWriteDtoService<SysSystemMappingDto, SysSystemMappingFilter> getService() {
		return mappingService;
	}
}
