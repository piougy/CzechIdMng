package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

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
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Persists remote server.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(RemoteServerSaveProcessor.PROCESSOR_NAME)
@Description("Persists remote server.")
public class RemoteServerSaveProcessor extends CoreEventProcessor<SysConnectorServerDto> {

	public static final String PROCESSOR_NAME = "acc-remote-server-save-processor";
	//
	@Autowired private SysRemoteServerService service;
	@Autowired private SysSystemService systemService;
	@Autowired private ConfidentialStorage confidentialStorage;
	
	public RemoteServerSaveProcessor() {
		super(RemoteServerEventType.UPDATE, RemoteServerEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysConnectorServerDto> process(EntityEvent<SysConnectorServerDto> event) {
		SysConnectorServerDto dto = event.getContent();
		GuardedString password = dto.getPassword();
		SysConnectorServerDto remoteServer = service.saveInternal(dto);
		//
		// after save entity save password to confidential storage
		// save password from remote connector server to confidential storage
		if (password != null) {
			// save for newSystem
			confidentialStorage.save(
					remoteServer.getId(), 
					SysRemoteServer.class, 
					SysSystemService.REMOTE_SERVER_PASSWORD,
					password.asString());
			//
			// set asterisks
			remoteServer.setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		//
		// update all systems => we need to be backward compatible (see SysSystemDto.getConnectorInstance() + password is loaded direcly from confidential storage for systems)
		SysSystemFilter systemFilter = new SysSystemFilter();
		systemFilter.setRemoteServerId(remoteServer.getId());
		systemService
			.find(systemFilter, null)
			.forEach(system -> {
				SysConnectorServerDto connectorServer = new SysConnectorServerDto(remoteServer);
				connectorServer.setPassword(password); // usable password
				//
				system.setConnectorServer(connectorServer);
				systemService.save(system);
			});
		//
		event.setContent(remoteServer);
		return new DefaultEventResult<>(event, this);
	}
}