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
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Delete given remote server.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(RemoteServerDeleteBulkAction.NAME)
@Description("Delete given remote server.")
public class RemoteServerDeleteBulkAction extends AbstractRemoveBulkAction<SysConnectorServerDto, SysRemoteServerFilter> {

	public static final String NAME = "acc-remote-server-delete-bulk-action";

	@Autowired private SysSystemService systemService;
	@Autowired private SysRemoteServerService remoteServerService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.REMOTESERVER_DELETE);
	}
	
	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();

		Map<ResultModel, Long> models = new HashMap<>();
		entities.forEach(remoteServerId -> {
			SysSystemFilter systemFilter = new SysSystemFilter();
			systemFilter.setRemoteServerId(remoteServerId);
			
			long count = systemService.count(systemFilter);
			if (count > 0) {
				SysConnectorServerDto remoteServer = getService().get(remoteServerId);
				
				models.put(new DefaultResultModel(AccResultCode.REMOTE_SYSTEM_DELETE_FAILED_HAS_SYSTEMS,
						ImmutableMap.of("remoteServer", remoteServer.getFullServerName(), "count", count)), count);
			}
		});
		//
		// Sort by count
		List<Entry<ResultModel, Long>> collect = models
				.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toList()); 
		collect.forEach(entry -> {
			result.addInfo(entry.getKey());
		});
		//
		return result;
	}

	@Override
	public ReadWriteDtoService<SysConnectorServerDto, SysRemoteServerFilter> getService() {
		return remoteServerService;
	}
}
