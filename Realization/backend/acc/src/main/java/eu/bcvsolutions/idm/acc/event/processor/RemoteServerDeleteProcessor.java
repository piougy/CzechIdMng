package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRemoteServer;
import eu.bcvsolutions.idm.acc.event.RemoteServerEvent.RemoteServerEventType;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;

/**
 * Delete remote server - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(RemoteServerDeleteProcessor.PROCESSOR_NAME)
@Description("Delete remote server - ensures referential integrity.")
public class RemoteServerDeleteProcessor extends CoreEventProcessor<SysConnectorServerDto> {

	public static final String PROCESSOR_NAME = "acc-remote-server-delete-processor";
	@Autowired private SysRemoteServerService service;
	@Autowired private SysSystemService systemService;
	@Autowired private ConfidentialStorage confidentialStorage;

	public RemoteServerDeleteProcessor() {
		super(RemoteServerEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysConnectorServerDto> process(EntityEvent<SysConnectorServerDto> event) {
		SysConnectorServerDto remoteServer = event.getContent();
		UUID remoteServerId = remoteServer.getId();
		Assert.notNull(remoteServerId, "Remove server identifier is required.");
		//
		// Check system not exists
		SysSystemFilter systemFilter = new SysSystemFilter();
		systemFilter.setRemoteServerId(remoteServerId);
		if (systemService.count(systemFilter) > 0) {
			throw new ResultCodeException(AccResultCode.REMOTE_SYSTEM_DELETE_FAILED_HAS_SYSTEMS,
					ImmutableMap.of("remoteServer", remoteServer.getFullServerName()));
		}
		//
		// deletes all confidential values
		confidentialStorage.deleteAll(remoteServerId, SysRemoteServer.class);
		//
		// deletes identity
		service.deleteInternal(remoteServer);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
