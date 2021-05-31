package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.AccRoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleCatalogueAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleCatalogueEvent.RoleCatalogueEventType;

/**
 * Deletes role catalogue items.
 * 
 * @author Radek Tomiška
 */
@Component("accRoleCatalogueDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleCatalogueDeleteProcessor extends CoreEventProcessor<IdmRoleCatalogueDto> {

	private static final String PROCESSOR_NAME = "role-catalogue-delete-processor";
	private final AccRoleCatalogueAccountService catalogueAccountService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	
	@Autowired
	public RoleCatalogueDeleteProcessor(AccRoleCatalogueAccountService service) {
		super(RoleCatalogueEventType.DELETE);
		//
		Assert.notNull(service, "Service is required.");
		//
		this.catalogueAccountService = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCatalogueDto> process(EntityEvent<IdmRoleCatalogueDto> event) {
		AccRoleCatalogueAccountFilter filter = new AccRoleCatalogueAccountFilter();
		filter.setEntityId(event.getContent().getId());
		catalogueAccountService.find(filter, null).forEach(treeAccount -> {
			catalogueAccountService.delete(treeAccount);
		});

		List<AbstractSysSyncConfigDto> syncConfigs = syncConfigService.findRoleConfigByMainCatalogueRoleNode(event.getContent().getId());
		checkSyncs(event, syncConfigs);

		syncConfigs = syncConfigService.findRoleConfigByRemoveCatalogueRoleParentNode(event.getContent().getId());
		checkSyncs(event, syncConfigs);

		return new DefaultEventResult<>(event, this);
	}

	private void checkSyncs(EntityEvent<IdmRoleCatalogueDto> event, List<AbstractSysSyncConfigDto> syncConfigs) {
		if (syncConfigs.size() > 0){
			SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(syncConfigs.get(0), SysSyncRoleConfig_.systemMapping, SysSystemMappingDto.class);
			SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
			SysSystemDto systemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);

			throw new ResultCodeException(AccResultCode.ROLE_CATALOGUE_DELETE_FAILED_USED_IN_SYNC,
					ImmutableMap.of("catalogue", event.getContent().getName(), "system", systemDto.getName()));
		}
	}

	@Override
	public int getOrder() {
		// right now before entity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}

}
