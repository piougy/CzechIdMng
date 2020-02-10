package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Delete given systems
 *
 * @author Ondrej Husnik
 *
 */
@Component(SystemMappingDeleteBulkAction.NAME)
@Description("Delete given system mapping")
public class SystemMappingDeleteBulkAction extends AbstractRemoveBulkAction<SysSystemMappingDto, SysSystemMappingFilter> {

	public static final String NAME = "acc-system-mapping-delete-bulk-action";

	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSyncConfigService synchronizationService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.SYSTEM_UPDATE);
	}

	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();
		Map<ResultModel, Long> models = new HashMap<>();

		entities.forEach(mappingId -> {
			SysSystemMappingDto mapping = getService().get(mappingId);

			SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
			syncFilter.setSystemMappingId(mappingId);
			long count = synchronizationService.count(syncFilter);
			if (count > 0) {
				models.put(new DefaultResultModel(AccResultCode.SYSTEM_MAPPING_DELETE_BULK_ACTION_MAPPING_IN_USE,
						ImmutableMap.of("mapping", mapping.getName(), "count", count)), count);
			}
		});
		
		List<Entry<ResultModel, Long>> collect = models //
				.entrySet() //
				.stream() //
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
				.collect(Collectors.toList());
		collect.forEach(entry -> {
			result.addInfo(entry.getKey());
		});

		return result;
	}

	@Override
	public ReadWriteDtoService<SysSystemMappingDto, SysSystemMappingFilter> getService() {
		return mappingService;
	}
}
